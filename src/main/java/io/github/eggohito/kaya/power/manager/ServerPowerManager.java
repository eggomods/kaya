package io.github.eggohito.kaya.power.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.kaya.action.manager.ActionManager;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.event.PowerPreparation;
import io.github.eggohito.kaya.event.PowerReloadEvents;
import io.github.eggohito.kaya.event.ReloadableServerResourcesEvents;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundUpdatePowersPacket;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.power.custom.MultiplePower;
import io.github.eggohito.kaya.registry.KayaRegistryKeys;
import io.github.eggohito.kaya.resource.json.JsonFileToIdConverter;
import io.github.eggohito.kaya.resource.json.JsonWithSource;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.Reporter;
import io.github.eggohito.kaya.util.ResourceLocationUtil;
import io.github.eggohito.kaya.util.manager.AbstractContentAndTagManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class ServerPowerManager extends AbstractContentAndTagManager<PowerIdentifier, PowerHolder<?>> implements PowerManager, IdentifiableResourceReloadListener {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final JsonFileToIdConverter contentLoader = JsonFileToIdConverter.registry(KayaRegistryKeys.POWER);
	private final TagLoader<PowerHolder<?>> tagLoader = new TagLoader<>((id, required) -> this.getAsResult(PowerIdentifier.of(id)).result(), Registries.tagsDirPath(KayaRegistryKeys.POWER));

	private volatile Map<ResourceLocation, List<TagLoader.EntryWithSource>> pendingTags = Map.of();
	private volatile DynamicOps<JsonElement> ops = JsonOps.INSTANCE;

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return DEPENDENCIES.freezeAndGet();
	}

	@Override
	public @NotNull CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {

		CompletableFuture<Map<ResourceLocation, JsonWithSource>> pendingPowers = CompletableFuture
			.supplyAsync(() -> MiscUtil.collectJson(manager, contentLoader, ops, LOGGER::error), backgroundExecutor);
		CompletableFuture<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> pendingTags = CompletableFuture
			.supplyAsync(() -> tagLoader.load(manager), backgroundExecutor);

		return pendingPowers.thenCombine(pendingTags, Pair::of)
			.thenCompose(barrier::wait)
			.thenAcceptAsync(pair -> this.apply(manager, Profiler.get(), pair.getFirst(), pair.getSecond()), gameExecutor);

	}

	@Override
	public void init() {

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ID, this::withOps);
		DEPENDENCIES.add(ActionManager.ID);

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(ActionManager.ID, ID);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ID, (player, joined) -> {

			if (!joined) {
				this.send(player);
			}

		});

		ServerPlayConnectionEvents.INIT.addPhaseOrdering(ActionManager.ID, ID);
		ServerPlayConnectionEvents.INIT.register((handler, server) -> this.send(handler.player));

		PowerPreparation.EVENT.addPhaseOrdering(PowerManager.ID, MultiplePower.ID);
		PowerPreparation.EVENT.register(PowerManager.ID, (id, jsonWithSource, directoryPath, ops) -> {

			if (jsonWithSource.json() instanceof JsonObject jsonObject) {
				jsonObject.addProperty(PowerHolder.ID_KEY, id.toString());
			}

		});

		PowerPreparation.EVENT.register(MultiplePower.ID, MultiplePower::preProcessSubPowers);

		ReloadableServerResourcesEvents.TAGS_UPDATED.addPhaseOrdering(ActionManager.ID, ID);
		ReloadableServerResourcesEvents.TAGS_UPDATED.register(ID, this::finalize);

	}

	protected void register(PowerHolder<?> holder, BiConsumer<PowerIdentifier, PowerHolder<?>> handler) {

		handler.accept(holder.id(), holder);

		if (holder.value() instanceof MultiplePower(ImmutableSet<PowerHolder<?>> subPowers, Power.Properties ignoredProperties)) {
			subPowers.forEach(subPower -> this.register(subPower, handler));
		}

	}

	private ServerPowerManager withOps(@NotNull HolderLookup.Provider provider) {
		this.ops = provider.createSerializationContext(JsonOps.INSTANCE);
		return this;
	}

	private void send(ServerPlayer recipient) {
		ServerPlayNetworking.send(recipient, new ClientboundUpdatePowersPacket(this.contents, this.tags));
	}

	private void apply(ResourceManager manager, ProfilerFiller profiler, Map<ResourceLocation, JsonWithSource> pendingPowers, Map<ResourceLocation, List<TagLoader.EntryWithSource>> pendingTags) {

		PowerReloadEvents.BEFORE.invoker().beforeReload(manager, profiler);
		LOGGER.info("Parsing powers from data packs...");

		ImmutableMap.Builder<PowerIdentifier, PowerHolder<?>> builder = ImmutableMap.builder();
		this.contents = ImmutableMap.of();

		pendingPowers.forEach((id, jsonWithSource) -> {

			PowerPreparation.EVENT.invoker().prepare(id, jsonWithSource, contentLoader.directory(), ops);

			PowerIdentifier powerId = PowerIdentifier.of(id);
			ResourceLocationUtil.setCurrent(id);

			MiscUtil.handleResult(
				PowerHolder.CODEC.parse(ops, jsonWithSource.json()),
				holder -> this.register(holder, builder::put),
				holder -> holder.value().canBePartiallyParsed(),
				warning -> LOGGER.warn("Found warning(s) while parsing {} from data pack [{}]: {}", powerId.asDisplayString(false), jsonWithSource.source(), warning),
				error -> LOGGER.error("Error trying to parse {} from data pack [{}] (skipping): {}", powerId.asDisplayString(false), jsonWithSource.source(), error)
			);

			ResourceLocationUtil.setCurrent(null);

		});

		this.contents = builder.build();
		this.pendingTags = pendingTags;

		PowerReloadEvents.AFTER.invoker().afterReload(manager, profiler);
		LOGGER.info("Finished parsing powers from data packs. Parsed {} power(s)", contents.size());

	}

	private void finalize(ReloadableServerResources resources) {

		ImmutableMap.Builder<PowerIdentifier, PowerHolder<?>> validatedContents = ImmutableMap.builder();
		int prevSize = contents.size();

		LOGGER.info("Validating {} power(s)...", prevSize);

		for (var holder : contents.values()) {

			try (Reporter.Scoped reporter = new Reporter.Scoped("{\"" + holder.id() + "\"}", problems -> LOGGER.error("Found problems while validating {}:\n{}", holder.id().asDisplayString(false), problems))) {

				ContextValidator validator = new ContextValidator(ContextValidator.Parameters.EMPTY, reporter, MiscUtil.getRegistries(resources));
				holder.validate(validator);

				if (!reporter.hasProblems()) {
					validatedContents.put(holder.id(), holder);
				}

			}

		}

		this.contents = validatedContents.build();
		LOGGER.info("Finished validating {} power(s). Power manager contains {} power(s)", prevSize, contents.size());

		LOGGER.info("Parsing power tags from data packs...");
		this.tags = ImmutableMap.copyOf(tagLoader.build(pendingTags));

		LOGGER.info("Finished parsing power tags from data packs. Power manager contains {} power tag(s)", tags.size());
		this.pendingTags = Map.of();

	}

	@Override
	public String toString() {
		return "Power manager";
	}

}
