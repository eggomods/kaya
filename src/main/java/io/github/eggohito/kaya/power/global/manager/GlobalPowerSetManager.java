package io.github.eggohito.kaya.power.global.manager;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.event.DependencyManager;
import io.github.eggohito.kaya.event.ReloadableServerResourcesEvents;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.entity.MutablePowers;
import io.github.eggohito.kaya.power.global.GlobalPowerSet;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.registry.KayaRegistryKeys;
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
import net.minecraft.Util;
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
import java.util.function.Supplier;

public final class GlobalPowerSetManager extends AbstractContentManager<ResourceLocation, GlobalPowerSet> implements IdentifiableResourceReloadListener {

	private static final GlobalPowerSetManager INSTANCE = new GlobalPowerSetManager();

	public static final ResourceLocation ID = Kaya.id("manager/global_power_set");
	public static final Supplier<ImmutableSet<ResourceLocation>> DEPENDENCIES = Suppliers.memoize(() -> Util.make(ImmutableSet.builder(), DependencyManager.GLOBAL_POWER_SETS.invoker()::add).build());

	private static final JsonFileToIdConverter LOADER = JsonFileToIdConverter.registry(KayaRegistryKeys.GLOBAL_POWER_SET);
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalPowerSetManager.class);

	private volatile ImmutableMap<ResourceLocation, List<GlobalPowerSet.File>> pendingContents = ImmutableMap.of();
	private DynamicOps<JsonElement> ops = JsonOps.INSTANCE;

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
		return DEPENDENCIES.get();
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
		DependencyManager.GLOBAL_POWER_SETS.register(ID, dependencies -> dependencies.add(PowerManager.ID));

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

		Map<ResourceLocation, List<GlobalPowerSet.File>> builder = new Object2ObjectLinkedOpenHashMap<>();
		LOGGER.info("Parsing global power sets from data packs...");

		pending.forEach((id, jsonWithSources) -> {

			ResourceLocationUtil.setCurrent(id);
			jsonWithSources.forEach(jsonWithSource -> GlobalPowerSet.Template.CODEC.parse(ops, jsonWithSource.json())
				.ifError(error -> LOGGER.error("Error trying to parse global power set \"{}\" from data pack [{}] (skipping): {}", id, jsonWithSource.source(), error.message()))
				.ifSuccess(template -> builder
					.computeIfAbsent(id, ignored -> new ObjectArrayList<>())
					.add(new GlobalPowerSet.File(id, jsonWithSource.source(), template))));

			ResourceLocationUtil.setCurrent(null);

		});

		this.pendingContents = ImmutableMap.copyOf(builder);
		LOGGER.info("Finished parsing global power sets. Parsed {} global power set(s)", this.pendingContents.size());

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

		ImmutableMap.Builder<ResourceLocation, GlobalPowerSet> builder = ImmutableMap.builder();

		for (var entry : pendingContents.entrySet()) {

			ResourceLocation id = entry.getKey();
			List<GlobalPowerSet.File> files = entry.getValue();

			List<GlobalPowerSet.File> pendingMerge = new ObjectArrayList<>();
			LOGGER.info("Validating {} file(s) for global power set \"{}\"...", files.size(), id);

			//  validate the global power sets before merging
			for (var file : files) {

				try (Reporter.Scoped reporter = new Reporter.Scoped("{\"" + id + "\"}", problems -> LOGGER.error("Found problems while validating global power set file \"{}\" from data pack [{}]:\n{}", id, file.source(), problems))) {

					ContextValidator validator = new ContextValidator(ContextValidator.Parameters.EMPTY, reporter, MiscUtil.getRegistries(resources));
					file.validate(validator);

					if (!reporter.hasProblems()) {
						pendingMerge.add(file);
					}

				}

			}

			StringBuilder info = new StringBuilder();
			info.append("Finished validating files for global power set \"").append(id).append("\"");

			if (pendingMerge.size() > 1) {
				info.append(". Merging all ").append(pendingMerge.size()).append(" files into one...");
			}

			LOGGER.info(info.toString());

			//  merge similar global power sets
			pendingMerge
				.stream()
				.reduce(GlobalPowerSet.File::merge)
				.ifPresent(file -> builder.put(id, file.template().toSet()));

			if (pendingMerge.size() > 1) {
				LOGGER.info("Finished merging {} files for global power set \"{}\"", pendingMerge.size(), id);
			}

		}

		this.pendingContents = ImmutableMap.of();
		this.contents = builder.build();

		LOGGER.info("Global power set manager contains {} global power set(s)", this.size());

	}

	public static GlobalPowerSetManager getInstance() {
		return INSTANCE;
	}

}
