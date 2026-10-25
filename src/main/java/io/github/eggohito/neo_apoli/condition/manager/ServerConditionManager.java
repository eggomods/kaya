package io.github.eggohito.neo_apoli.condition.manager;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.event.DependencyManager;
import io.github.eggohito.neo_apoli.network.packet.clientbound.ClientboundUpdateConditionsPacket;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.resource.json.JsonFileToIdConverter;
import io.github.eggohito.neo_apoli.resource.json.JsonWithSource;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import io.github.eggohito.neo_apoli.util.manager.AbstractContentManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class ServerConditionManager extends AbstractContentManager<ResourceLocation, Condition> implements ConditionManager, IdentifiableResourceReloadListener {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Supplier<ImmutableSet<ResourceLocation>> DEPENDENCIES = Suppliers.memoize(() -> Util.make(ImmutableSet.builder(), DependencyManager.CONDITIONS.invoker()::add).build());

	private final JsonFileToIdConverter loader = JsonFileToIdConverter.registry(NeoApoliRegistryKeys.CONDITION);
	private volatile DynamicOps<JsonElement> ops = JsonOps.INSTANCE;

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return DEPENDENCIES.get();
	}

	@Override
	public @NotNull CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.supplyAsync(() -> MiscUtil.collectJson(manager, loader, ops, LOGGER::error), backgroundExecutor)
			.thenCompose(barrier::wait)
			.thenAcceptAsync(pending -> this.apply(manager, Profiler.get(), pending), gameExecutor);
	}

	@Override
	public void init() {

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ID, this::withOps);
		ServerPlayConnectionEvents.INIT.register(ID, (handler, server) -> this.send(handler.player));

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ID, (player, joined) -> {

			if (!joined) {
				this.send(player);
			}

		});

	}

	private ServerConditionManager withOps(@NotNull HolderLookup.Provider provider) {
		this.ops = provider.createSerializationContext(JsonOps.INSTANCE);
		return this;
	}

	private void send(ServerPlayer recipient) {
		ServerPlayNetworking.send(recipient, new ClientboundUpdateConditionsPacket(this.contents));
	}

	private void apply(ResourceManager manager, ProfilerFiller profiler, Map<ResourceLocation, JsonWithSource> pending) {

		LOGGER.info("Parsing conditions from data packs...");
		ImmutableMap.Builder<ResourceLocation, Condition> builder = ImmutableMap.builder();

		pending.forEach((id, jsonWithSource) -> {

			ResourceLocationUtil.setCurrent(id);
			MiscUtil.handleResult(
				Condition.CODEC.parse(ops, jsonWithSource.json()),
				condition -> builder.put(id, condition),
				warning -> LOGGER.warn("Found warning(s) while parsing condition \"{}\" from data pack [{}]: {}", id, jsonWithSource.source(), warning),
				error -> LOGGER.error("Error trying to parse condition \"{}\" from data pack [{}] (skipping): {}", id, jsonWithSource.source(), error)
			);

			ResourceLocationUtil.setCurrent(null);

		});

		this.contents = builder.build();
		LOGGER.info("Finished parsing conditions from data packs. Parsed {} condition(s)", contents.size());

	}

}
