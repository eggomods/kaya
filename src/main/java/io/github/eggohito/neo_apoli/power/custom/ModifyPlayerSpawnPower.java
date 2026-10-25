package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.config.AbstractJsonCodecConfig;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
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
import org.jetbrains.annotations.NotNull;
import org.quiltmc.parsers.json.JsonFormat;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public record ModifyPlayerSpawnPower(Optional<Condition> activeCondition, ResourceKey<Level> dimension, Optional<Either<ResourceKey<Biome>, TagKey<Biome>>> biome, Optional<Either<ResourceKey<Structure>, TagKey<Structure>>> structure, int priority) implements PrioritizedPower<ModifyPlayerSpawnPower> {

	public static final MapCodec<ModifyPlayerSpawnPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(ModifyPlayerSpawnPower::dimension))
		.and(NeoApoliCodecs.BIOME_KEY_OR_TAG.optionalFieldOf("biome").forGetter(ModifyPlayerSpawnPower::biome))
		.and(NeoApoliCodecs.STRUCTURE_KEY_OR_TAG.optionalFieldOf("structure").forGetter(ModifyPlayerSpawnPower::structure))
		.and(Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyPlayerSpawnPower::priority))
		.apply(instance, ModifyPlayerSpawnPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyPlayerSpawnPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), ModifyPlayerSpawnPower::activeCondition,
		ResourceKey.streamCodec(Registries.DIMENSION), ModifyPlayerSpawnPower::dimension,
		ByteBufCodecs.optional(NeoApoliStreamCodecs.BIOME_KEY_OR_TAG), ModifyPlayerSpawnPower::biome,
		ByteBufCodecs.optional(NeoApoliStreamCodecs.STRUCTURE_KEY_OR_TAG), ModifyPlayerSpawnPower::structure,
		ByteBufCodecs.INT, ModifyPlayerSpawnPower::priority,
		ModifyPlayerSpawnPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_PLAYER_SPAWN;
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

		private static final MapCodec<Optional<ServerPlayer.RespawnConfig>> LOCATION_MAP_CODEC = MapCodec.assumeMapUnsafe(ExtraCodecs.optionalEmptyMap(ServerPlayer.RespawnConfig.CODEC));

		private CompletableFuture<TeleportTransition> respawnTeleport = new CompletableFuture<>();
		private Optional<ServerPlayer.RespawnConfig> respawnLocation = Optional.empty();

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
			CompletableFuture<TeleportTransition> respawnLocation = this.getRespawnLocation();
			return respawnLocation.isDone()
				? respawnLocation
				: this.findRespawnLocation(player);
		}

		public CompletableFuture<TeleportTransition> findRespawnLocation(ServerPlayer player) {
			return this.respawnTeleport = CompletableFuture
				.supplyAsync(() -> this.findRespawnLocationInternal(player), Util.ioPool()) // Using Minecraft's IO thread pool should be fine?
				.thenApply(this::onLocationFound);
		}

		private TeleportTransition onLocationFound(Pair<ServerLevel, BlockPos> pair) {

			ServerLevel level = pair.getFirst();
			BlockPos blockPos = pair.getSecond();

			var newTeleport = new TeleportTransition(level, blockPos.getBottomCenter(), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING);
			this.respawnLocation = Optional.of(new ServerPlayer.RespawnConfig(level.dimension(), blockPos, 0.0F, false));

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

			return Pair.of(dimension, MiscUtil.adjustSpawnLocationSafely(player, dimension, spawnPos));

		}

		private BlockPos findBiomeLocation(ServerLevel dimension, BlockPos pos, int horizontalSteps, int verticalSteps, int radius) {

			var foundBiome = dimension.findClosestBiome3d(
				biomeHolder -> power.biome().isEmpty() || power.biome().get().map(biomeHolder::is, biomeHolder::is),
				pos,
				radius,
				horizontalSteps,
				verticalSteps
			);

			if (foundBiome != null) {
				return foundBiome.getFirst();
			}

			else {
				return pos;
			}

		}

		private BlockPos findStructureLocation(ServerLevel dimension, BlockPos pos, int radius) {

			if (power.structure().isEmpty()) {
				return pos;
			}

			Registry<Structure> registry = dimension.registryAccess().lookupOrThrow(Registries.STRUCTURE);
			HolderSet<Structure> structures = power.structure().get().map(key -> HolderSet.direct(registry.getOrThrow(key)), registry::getOrThrow);

			var foundStructure = dimension.getChunkSource().getGenerator().findNearestMapStructure(
				dimension,
				structures,
				pos,
				radius,
				false
			);

			if (foundStructure != null) {
				return foundStructure.getFirst().mutable().setY(pos.getY());
			}

			else {
				return pos;
			}

		}

	}

	public static final class Config extends AbstractJsonCodecConfig<Config> {

		public static final Config INSTANCE = new Config();

		public final ConfigEntry<Integer> horizontalStep = register("horizontal_step", 64, ExtraCodecs.NON_NEGATIVE_INT);
		public final ConfigEntry<Integer> verticalStep = register("vertical_step", 64, ExtraCodecs.NON_NEGATIVE_INT);
		public final ConfigEntry<Integer> radius = register("radius", 6400, ExtraCodecs.NON_NEGATIVE_INT);

		Config() {
			super(FabricLoader.getInstance().getConfigDir().resolve("neo-apoli/type/power/modify_player_spawn.json5"), JsonFormat.JSON5);
		}

	}

}
