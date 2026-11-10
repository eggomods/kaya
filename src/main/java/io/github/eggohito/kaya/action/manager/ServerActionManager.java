package io.github.eggohito.kaya.action.manager;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.action.ActionHolder;
import io.github.eggohito.kaya.condition.manager.ConditionManager;
import io.github.eggohito.kaya.event.ReloadableServerResourcesEvents;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundUpdateActionsPacket;
import io.github.eggohito.kaya.registry.KayaRegistryKeys;
import io.github.eggohito.kaya.resource.json.JsonFileToIdConverter;
import io.github.eggohito.kaya.resource.json.JsonWithSource;
import io.github.eggohito.kaya.util.MiscUtil;
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

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class ServerActionManager extends AbstractContentAndTagManager<ResourceLocation, ActionHolder<?>> implements ActionManager, IdentifiableResourceReloadListener {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final JsonFileToIdConverter contentLoader = JsonFileToIdConverter.registry(KayaRegistryKeys.ACTION);
	private final TagLoader<ActionHolder<?>> tagLoader = new TagLoader<>((id, required) -> this.getAsResult(id).result(), Registries.tagsDirPath(KayaRegistryKeys.ACTION));

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

		CompletableFuture<Map<ResourceLocation, JsonWithSource>> pendingActions = CompletableFuture
			.supplyAsync(() -> MiscUtil.collectJson(manager, contentLoader, ops, LOGGER::error), backgroundExecutor);
		CompletableFuture<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> pendingTags = CompletableFuture
			.supplyAsync(() -> tagLoader.load(manager), backgroundExecutor);

		return pendingActions.thenCombine(pendingTags, Pair::of)
			.thenCompose(barrier::wait)
			.thenAcceptAsync(pair -> this.apply(manager, Profiler.get(), pair.getFirst(), pair.getSecond()), gameExecutor);

	}

	@Override
	public void init() {

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ID, this::withOps);
		DEPENDENCIES.add(ConditionManager.ID);

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(ConditionManager.ID, ID);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ID, (player, joined) -> {

			if (!joined) {
				this.send(player);
			}

		});

		ServerPlayConnectionEvents.INIT.addPhaseOrdering(ConditionManager.ID, ID);
		ServerPlayConnectionEvents.INIT.register(ActionManager.ID, (handler, server) -> this.send(handler.player));

		ReloadableServerResourcesEvents.TAGS_UPDATED.addPhaseOrdering(ConditionManager.ID, ID);
		ReloadableServerResourcesEvents.TAGS_UPDATED.register(ID, this::finalize);

	}

	private ServerActionManager withOps(@NotNull HolderLookup.Provider provider) {
		this.ops = provider.createSerializationContext(JsonOps.INSTANCE);
		return this;
	}

	private void send(ServerPlayer recipient) {
		ServerPlayNetworking.send(recipient, new ClientboundUpdateActionsPacket(this.contents, this.tags));
	}

	private void apply(ResourceManager manager, ProfilerFiller profiler, Map<ResourceLocation, JsonWithSource> pendingActions, Map<ResourceLocation, List<TagLoader.EntryWithSource>> pendingTags) {

		LOGGER.info("Parsing actions from data packs...");

		ImmutableMap.Builder<ResourceLocation, ActionHolder<?>> builder = ImmutableMap.builder();
		this.contents = ImmutableMap.of();

		pendingActions.forEach((id, jsonWithSource) -> {

			ResourceLocationUtil.setCurrent(id);
			MiscUtil.handleResult(
				Action.CODEC.parse(ops, jsonWithSource.json()),
				action -> builder.put(id, new ActionHolder<>(id, action)),
				warning -> LOGGER.warn("Found warning(s) while parsing action \"{}\" from data pack [{}]: {}", id, jsonWithSource.source(), warning),
				error -> LOGGER.error("Error trying to parse action \"{}\" from data pack [{}] (skipping): {}", id, jsonWithSource.source(), error)
			);

			ResourceLocationUtil.setCurrent(null);

		});

		this.contents = builder.build();
		this.pendingTags = pendingTags;

		LOGGER.info("Finished parsing actions from data packs. Action manager contains {} action(s)", contents.size());

	}

	private void finalize(ReloadableServerResources resources) {

		LOGGER.info("Parsing action tags from data packs...");
		this.tags = ImmutableMap.copyOf(tagLoader.build(pendingTags));

		LOGGER.info("Finished parsing action tags from data packs. Action manager contains {} action tag(s)", tags.size());
		this.pendingTags = Map.of();

	}

	@Override
	public String toString() {
		return "Action manager";
	}

}
