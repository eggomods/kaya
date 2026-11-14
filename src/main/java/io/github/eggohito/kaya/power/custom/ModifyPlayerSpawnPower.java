package io.github.eggohito.kaya.power.custom;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.config.AbstractJsonCodecConfig;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.RegistryUtil;
import io.github.eggohito.kaya.util.Reporter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.parsers.json.JsonFormat;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public record ModifyPlayerSpawnPower(ResourceKey<Level> dimension, Optional<Either<ResourceKey<Biome>, TagKey<Biome>>> biome, Optional<Either<ResourceKey<Structure>, TagKey<Structure>>> structure, int priority, Properties properties) implements PrioritizedPower<ModifyPlayerSpawnPower> {

	public static final MapCodec<ModifyPlayerSpawnPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(ModifyPlayerSpawnPower::dimension),
		KayaCodecs.BIOME_KEY_OR_TAG.optionalFieldOf("biome").forGetter(ModifyPlayerSpawnPower::biome),
		KayaCodecs.STRUCTURE_KEY_OR_TAG.optionalFieldOf("structure").forGetter(ModifyPlayerSpawnPower::structure),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyPlayerSpawnPower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyPlayerSpawnPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyPlayerSpawnPower> STREAM_CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(Registries.DIMENSION), ModifyPlayerSpawnPower::dimension,
		ByteBufCodecs.optional(KayaStreamCodecs.BIOME_KEY_OR_TAG), ModifyPlayerSpawnPower::biome,
		ByteBufCodecs.optional(KayaStreamCodecs.STRUCTURE_KEY_OR_TAG), ModifyPlayerSpawnPower::structure,
		ByteBufCodecs.INT, ModifyPlayerSpawnPower::priority,
		Properties.STREAM_CODEC, ModifyPlayerSpawnPower::properties,
		ModifyPlayerSpawnPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_PLAYER_SPAWN;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		RegistryUtil.validateKey(validator.forChild(".dimension"), this.dimension());
		this.biome().ifPresent(biome -> RegistryUtil.validateKeyOrTag(validator.forChild(".biome"), biome));
		this.structure().ifPresent(structure -> RegistryUtil.validateKeyOrTag(validator.forChild(".structure"), structure));
	}

	public static class Instance extends Power.Instance<ModifyPlayerSpawnPower> {

		private static final MapCodec<Optional<LocationData>> LOCATION_MAP_CODEC = MapCodec.assumeMapUnsafe(ExtraCodecs.optionalEmptyMap(LocationData.CODEC));

		private CompletableFuture<TeleportTransition> respawnTeleport = new CompletableFuture<>();
		private Optional<LocationData> respawnLocation = Optional.empty();

		protected Instance(@NotNull ModifyPlayerSpawnPower power) {
			super(power);
		}

		@Override
		public void onGranted(Entity holder) {

			super.onGranted(holder);

			if (holder instanceof ServerPlayer player) {
				this.getOrFindRespawnLocation(player);
			}

		}

		@Override
		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {
			return LOCATION_MAP_CODEC.decode(ops, mapInput)
				.ifSuccess(location -> this.respawnLocation = location)
				.map(ignored -> Unit.INSTANCE);
		}

		@Override
		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {
			return LOCATION_MAP_CODEC.encode(this.respawnLocation, ops, prefix);
		}

		public CompletableFuture<TeleportTransition> getRespawnLocation() {
			return respawnTeleport;
		}

		public CompletableFuture<TeleportTransition> getOrFindRespawnLocation(ServerPlayer player) {

			CompletableFuture<TeleportTransition> respawnTeleport = this.getRespawnLocation();

			if (!respawnTeleport.isDone()) {
				this.respawnLocation
					.map(locationData -> locationData.prepareTeleport(player.server))
					.ifPresent(respawnTeleport::complete);
			}

			return respawnTeleport.isDone()
				? respawnTeleport
				: this.findRespawnLocation(player);

		}

		public CompletableFuture<TeleportTransition> findRespawnLocation(ServerPlayer player) {
			return this.respawnTeleport = CompletableFuture
				.supplyAsync(() -> this.findRespawnLocationInternal(player), Util.ioPool()) // Using Minecraft's IO thread pool should be fine?
				.thenApply(this::onLocationFound);
		}

		public Context createContext(Entity holder, Reporter reporter) {
			return this.createHolderContextBuilder(holder)
				.withReporter(reporter.forChild("{\"" + this.id() + "\"}"))
				.build(holder.level());
		}

		private TeleportTransition onLocationFound(Pair<ServerLevel, BlockPos> pair) {

			ServerLevel level = pair.getFirst();
			BlockPos blockPos = pair.getSecond();

			var newTeleport = new TeleportTransition(level, blockPos.getBottomCenter(), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING);
			this.respawnLocation = Optional.of(new LocationData(level.dimension(), blockPos));

			return newTeleport;

		}

		private Pair<ServerLevel, BlockPos> findRespawnLocationInternal(ServerPlayer player) {

			MinecraftServer server = player.server;
			ServerLevel dimension = server.getLevel(power.dimension());

			if (dimension == null) {
				throw new IllegalStateException("Dimension \"" + power.dimension().location() + "\" doesn't exist!");
			}

			BlockPos spawnPos = dimension.getSharedSpawnPos();

			int horizontalStep = Config.INSTANCE.horizontalStep.get();
			int verticalStep = Config.INSTANCE.verticalStep.get();
			int radius = Config.INSTANCE.radius.get();

			spawnPos = this.findBiomeLocation(dimension, spawnPos, horizontalStep, verticalStep, radius);
			spawnPos = this.findStructureLocation(dimension, spawnPos, radius);

			BlockPos adjustedPos = MiscUtil.adjustSpawnLocationSafely(player, dimension, spawnPos);

			if (adjustedPos == null) {
				throw new CompletionException("Could not find a safe location at " + spawnPos.toShortString(), null);
			}

			else {
				return Pair.of(dimension, adjustedPos);
			}

		}

		private BlockPos findBiomeLocation(ServerLevel level, BlockPos pos, int horizontalSteps, int verticalSteps, int radius) {

			if (power.biome().isEmpty()) {
				return pos;
			}

			var foundBiome = level.findClosestBiome3d(
				biomeHolder -> power.biome().get().map(biomeHolder::is, biomeHolder::is),
				pos,
				radius,
				horizontalSteps,
				verticalSteps
			);

			if (foundBiome != null) {
				return foundBiome.getFirst();
			}

			else {

				String biome = power.biome().get().map(
					key -> "biome \"" + key.location() + "\"",
					tag -> "any biomes from tag \"#" + tag.location() + "\""
				);

				throw new CompletionException("Could not find " + biome + " in dimension \"" + level.dimension().location() + "\"", null);

			}

		}

		private BlockPos findStructureLocation(ServerLevel level, BlockPos pos, int radius) {

			if (power.structure().isEmpty()) {
				return pos;
			}

			Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
			HolderSet<Structure> structures = power.structure().get().map(key -> HolderSet.direct(registry.getOrThrow(key)), registry::getOrThrow);

			var foundStructure = level.getChunkSource().getGenerator().findNearestMapStructure(
				level,
				structures,
				pos,
				radius,
				false
			);

			if (foundStructure != null) {
				return foundStructure.getFirst().mutable().setY(pos.getY());
			}

			else {

				String structure = power.structure().get().map(
					key -> "structure \"" + key.location() + "\"",
					tag -> "any structures from tag \"#" + tag.location() + "\""
				);

				throw new CompletionException("Could not find " + structure + " in dimension \"" + level.dimension().location() + "\"", null);

			}

		}

	}

	public static TeleportTransition getSpawnLocationOrElse(ServerPlayer player, Supplier<TeleportTransition> defaultGetter) {

		MutableBoolean useDefault = new MutableBoolean();
		Reporter.Scoped reporter = new Reporter.Scoped(problems -> {

			StringBuilder builder = new StringBuilder()
				.append("Found problems while trying to find/get custom spawn point for player ")
				.append(player.getName().getString());

			if (useDefault.isTrue()) {
				builder.append(" (using default spawn point as fallback)");
			}

			builder.append(":\n").append(problems);
			Kaya.LOGGER.warn(builder.toString());

		});

		try (reporter) {

			for (var instance : new InstanceCollection<>(player, Instance.class)) {

				Context context = instance.createContext(player, reporter);
				CompletableFuture<TeleportTransition> respawnLocation = instance.getOrFindRespawnLocation(player);

				if (instance.isActive(context) && respawnLocation.isDone()) {

					TeleportTransition actualRespawnLocation = respawnLocation
						.exceptionally(throwable -> {
							context.reporter().report(throwable.getMessage());
							return null;
						})
						.join();

					if (actualRespawnLocation != null) {
						return actualRespawnLocation;
					}

				}

			}

			useDefault.setTrue();
			return defaultGetter.get();

		}

	}

	public record LocationData(ResourceKey<Level> dimension, BlockPos position) {

		private static final Codec<LocationData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(LocationData::dimension),
			BlockPos.CODEC.fieldOf("position").forGetter(LocationData::position)
		).apply(instance, LocationData::new));

		private static final Codec<LocationData> FROM_CONFIG_CODEC = ServerPlayer.RespawnConfig.CODEC.xmap(
			config -> new LocationData(config.dimension(), config.pos()),
			location -> new ServerPlayer.RespawnConfig(location.dimension(), location.position(), 0.0F, false)
		);

		public static final Codec<LocationData> CODEC = Codec.withAlternative(DIRECT_CODEC, FROM_CONFIG_CODEC);

		public TeleportTransition prepareTeleport(MinecraftServer server) {
			return new TeleportTransition(
				Objects.requireNonNull(server.getLevel(this.dimension()), "Dimension \"" + this.dimension().location() + "\" doesn't exist!"),
				this.position().getBottomCenter(),
				Vec3.ZERO,
				0.0F,
				0.0F,
				TeleportTransition.DO_NOTHING
			);
		}

	}

	public static final class Config extends AbstractJsonCodecConfig<Config> {

		public static final Config INSTANCE = new Config();

		public final ConfigEntry<Integer> horizontalStep = register("horizontal_step", 64, ExtraCodecs.NON_NEGATIVE_INT);
		public final ConfigEntry<Integer> verticalStep = register("vertical_step", 64, ExtraCodecs.NON_NEGATIVE_INT);
		public final ConfigEntry<Integer> radius = register("radius", 6400, ExtraCodecs.NON_NEGATIVE_INT);

		Config() {
			super(FabricLoader.getInstance().getConfigDir().resolve("kaya/type/power/modify_player_spawn.json5"), JsonFormat.JSON5);
		}

	}

}
