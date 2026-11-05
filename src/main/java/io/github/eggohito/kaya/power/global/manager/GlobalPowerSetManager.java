package io.github.eggohito.kaya.power.global.manager;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.event.ReloadableServerResourcesEvents;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.entity.MutablePowers;
import io.github.eggohito.kaya.power.global.GlobalPowerSet;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.registry.KayaRegistryKeys;
import io.github.eggohito.kaya.resource.DependencyList;
import io.github.eggohito.kaya.resource.json.JsonFileToIdConverter;
import io.github.eggohito.kaya.resource.json.JsonWithSource;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.Reporter;
import io.github.eggohito.kaya.util.ResourceLocationUtil;
import io.github.eggohito.kaya.util.manager.AbstractContentManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class GlobalPowerSetManager extends AbstractContentManager<ResourceLocation, GlobalPowerSet> implements IdentifiableResourceReloadListener {

	private static final GlobalPowerSetManager INSTANCE = new GlobalPowerSetManager();

	public static final ResourceLocation ID = Kaya.id("manager/global_power_set");
	public static final DependencyList DEPENDENCIES = new DependencyList(ID);

	private static final JsonFileToIdConverter LOADER = JsonFileToIdConverter.registry(KayaRegistryKeys.GLOBAL_POWER_SET);
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalPowerSetManager.class);

	private volatile ImmutableMap<ResourceLocation, GlobalPowerSet.SourcedFile> merged = ImmutableMap.of();
	private volatile DynamicOps<JsonElement> ops = JsonOps.INSTANCE;

	private GlobalPowerSetManager() {

	}

	@Override
	public @NotNull CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.supplyAsync(() -> MiscUtil.collectJsonStack(manager, LOADER, ops, LOGGER::error), backgroundExecutor)
			.thenCompose(barrier::wait)
			.thenAcceptAsync(this::apply, gameExecutor);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return DEPENDENCIES.freezeAndGet();
	}

	public List<GlobalPowerSet> getApplicableSets(Entity entity) {

		List<GlobalPowerSet> applicable = new ObjectArrayList<>();

		for (var globalPower : values()) {

			if (globalPower.doesApply(entity)) {
				applicable.add(globalPower);
			}

		}

		return applicable;

	}

	@ApiStatus.Internal
	public void init() {

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ID, this::withContext);
		DEPENDENCIES.add(PowerManager.ID);

		ReloadableServerResourcesEvents.TAGS_UPDATED.addPhaseOrdering(PowerManager.ID, ID);
		ReloadableServerResourcesEvents.TAGS_UPDATED.register(ID, this::finalize);

		ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(PowerManager.ID, ID);
		ServerEntityEvents.ENTITY_LOAD.register(ID, this::grant);

	}

	private GlobalPowerSetManager withContext(@NotNull HolderLookup.Provider provider) {
		this.ops = provider.createSerializationContext(JsonOps.INSTANCE);
		return this;
	}

	private Set<PowerHolder<?>> flattenPowers(Collection<GlobalPowerSet> sets) {

		Set<PowerHolder<?>> holders = new ObjectOpenHashSet<>();
		for (var set : sets) {
			holders.addAll(set.powers().elements());
		}

		return holders;

	}

	private void apply(Map<ResourceLocation, List<JsonWithSource>> pending) {

		if (ops == null) {
			return;
		}

		LOGGER.info("Parsing global power sets from data packs...");
		Map<ResourceLocation, List<GlobalPowerSet.SourcedFile>> toMerge = new Object2ObjectLinkedOpenHashMap<>();

		pending.forEach((id, elementWithSources) -> {

			ResourceLocationUtil.setCurrent(id);
			elementWithSources.forEach(jsonWithSource -> GlobalPowerSet.File.CODEC.compressedDecode(ops, jsonWithSource.json())
				.ifError(error -> LOGGER.error("Error trying to parse global power set \"{}\" from data pack [{}] (skipping): {}", id, jsonWithSource.source(), error.message()))
				.ifSuccess(file -> toMerge
					.computeIfAbsent(id, ignored -> new ObjectArrayList<>())
					.add(new GlobalPowerSet.SourcedFile(file, jsonWithSource.source()))));

			ResourceLocationUtil.setCurrent(null);

		});

		LOGGER.info("Finished parsing global power sets. Merging {} similar global power sets...", toMerge.values().stream().mapToInt(Collection::size).sum());

		ImmutableMap.Builder<ResourceLocation, GlobalPowerSet.SourcedFile> builder = ImmutableMap.builder();
		toMerge.forEach((id, sourcedFiles) -> sourcedFiles
			.stream()
			.reduce((first, second) -> first.merge(second, () -> LOGGER.warn("Global power set \"{}\" from data pack [{}] has been replaced with a similar one from data pack [{}]!", id, first.source(), second.source())))
			.ifPresent(sourcedFile -> builder.put(id, sourcedFile)));

		this.merged = builder.build();
		LOGGER.info("Finished merging global power sets.");

	}

	private void grant(Entity entity, ServerLevel serverLevel) {

		try (MutablePowers mutable = MutablePowers.create(entity)) {

			List<GlobalPowerSet> applicableSets = this.getApplicableSets(entity);
			Set<PowerHolder<?>> expectedHolders = this.flattenPowers(applicableSets);

			//  Revoke all powers that are from the global power source, but not without the expected
			//  set of powers collected from all global power sets
			for (var holder : mutable.getAllFromSource(GlobalPowerSet.POWER_SOURCE)) {

				if (!expectedHolders.contains(holder)) {
					mutable.revoke(holder.id(), GlobalPowerSet.POWER_SOURCE);
				}

			}

			//  Re-grant all the expected powers collected from all global power sets
			for (var expectedHolder : expectedHolders) {
				mutable.grant(expectedHolder.id(), GlobalPowerSet.POWER_SOURCE);
			}

		}

	}

	private void finalize(ReloadableServerResources resources) {

		if (merged.isEmpty()) {
			return;
		}

		ImmutableMap.Builder<ResourceLocation, GlobalPowerSet> builder = ImmutableMap.builder();
		int size = merged.size();

		LOGGER.info("Validating {} global power set(s)...", size);

		for (var entry : merged.entrySet()) {

			ResourceLocation id = entry.getKey();
			GlobalPowerSet set = entry.getValue().asSet();

			try (Reporter.Scoped reporter = new Reporter.Scoped("{\"" + id + "\"}", problems -> LOGGER.error("Found problems while validating global power set \"{}\":\n{}", id, problems))) {

				ContextValidator validator = new ContextValidator(ContextValidator.Parameters.EMPTY, reporter, MiscUtil.getRegistries(resources));
				set.validate(validator);

				if (!reporter.hasProblems()) {
					builder.put(id, set);
				}

			}

		}

		this.merged = ImmutableMap.of();
		this.contents = builder.build();

		LOGGER.info("Finished validating {} global power set(s). Global power set manager contains {} global power set(s)", size, this.size());

	}

	public static GlobalPowerSetManager getInstance() {
		return INSTANCE;
	}

}
