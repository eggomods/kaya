package io.github.eggohito.kaya.util;

import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.*;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.mixin.access.RegistryOpsAccessor;
import io.github.eggohito.kaya.mixin.access.ReloadableServerRegistriesAccessor;
import io.github.eggohito.kaya.mixin.access.ServerPlayerAccessor;
import io.github.eggohito.kaya.resource.json.JsonFileToIdConverter;
import io.github.eggohito.kaya.resource.json.JsonWithSource;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.parsers.json.JsonFormat;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.gson.GsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.*;

public class MiscUtil {

	private static final Iterable<?> EMPTY_ITERABLE = () -> new AbstractIterator<>() {

		@Override
		protected Object computeNext() {
			return endOfData();
		}

	};

	public static final ImmutableMap<String, InteractionHand> HANDS = ImmutableMap.of(
		"mainhand", InteractionHand.MAIN_HAND,
		"offhand", InteractionHand.OFF_HAND
	);

	public static final ImmutableBiMap<String, InteractionResult> INTERACTION_RESULTS = ImmutableBiMap.of(
		"success", InteractionResult.SUCCESS,
		"success_server", InteractionResult.SUCCESS_SERVER,
		"consume", InteractionResult.CONSUME,
		"fail", InteractionResult.FAIL,
		"pass", InteractionResult.PASS
	);

	public static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.create();

	public static CommandSyntaxException createCommandException(Message message) {
		return new SimpleCommandExceptionType(message).create();
	}

	public static CommandSyntaxException createCommandExceptionWithContext(ImmutableStringReader reader, Message message) {
		return new SimpleCommandExceptionType(message).createWithContext(reader);
	}

	private static final MapCodec<Optional<ResourceCondition>> RESOURCE_CONDITION_MAP_CODEC = ResourceCondition.CONDITION_CODEC.optionalFieldOf(ResourceConditions.CONDITIONS_KEY);

	public static <I> boolean isResourceConditionFulfilled(ResourceLocation resourceId, I input, String directory, DynamicOps<I> ops) {
		return ops.getMap(input).mapOrElse(mapInput -> isResourceConditionFulfilled(resourceId, mapInput, directory, ops), error -> true);
	}

	public static <I> boolean isResourceConditionFulfilled(ResourceLocation resourceId, MapLike<I> mapInput, String directory, DynamicOps<I> ops) {

		RegistryOps.RegistryInfoLookup infoLookup = ops instanceof RegistryOps<I> registryOps
			? ((RegistryOpsAccessor) registryOps).getLookupProvider()
			: null;

		return RESOURCE_CONDITION_MAP_CODEC.decode(ops, mapInput)
			.ifError(error -> Kaya.LOGGER.error("Failed to parse resource conditions for file of type {} with ID '{}', skipping: {}", directory, resourceId, error.message()))
			.result()
			.flatMap(Function.identity())
			.map(condition -> condition.test(infoLookup))
			.orElse(true);

	}

	public static boolean isResultPass(InteractionResult result) {
		return (result instanceof InteractionResult.Success(InteractionResult.SwingSource swingSource, InteractionResult.ItemContext ignored) && swingSource == InteractionResult.SwingSource.NONE)
			|| result instanceof InteractionResult.TryEmptyHandInteraction
			|| result instanceof InteractionResult.Pass;
	}

	public static InteractionResult overrideResult(InteractionResult oldResult, InteractionResult newResult) {

		if (isResultPass(newResult)) {
			return oldResult;
		}

		else {
			return newResult;
		}

	}

	public static Set<ServerPlayer> getTrackingOrEmpty(Entity entity) {

		if (entity.level().isClientSide()) {
			return new ObjectOpenHashSet<>();
		}

		Set<ServerPlayer> trackers = new ObjectOpenHashSet<>(PlayerLookup.tracking(entity));

		if (entity instanceof ServerPlayer self) {
			trackers.add(self);
		}

		return trackers;

	}

	@Nullable
	public static CachedBlock getViewBlocking(Entity entity) {

		if (!(entity instanceof LivingEntity livingEntity)) {
			return null;
		}

		Level level = entity.level();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 8; i++) {

			double d = livingEntity.getX() + (i % 2 - 0.5F) * livingEntity.getBbWidth() * 0.8F;
			double e = livingEntity.getEyeY() + ((i >> 1) % 2 - 0.5F) * 0.1F * livingEntity.getScale();
			double f = livingEntity.getZ() + ((i >> 2) % 2 - 0.5F) * livingEntity.getBbWidth() * 0.8F;

			mutable.set(d, e, f);
			BlockState blockState = level.getBlockState(mutable);

			if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(level, mutable)) {
				return new CachedBlock(mutable, blockState, level.getBlockEntity(mutable));
			}

		}

		return null;

	}

	public static boolean hasFinishedReading(StringReader reader) {
		return !reader.canRead()
			|| Character.isWhitespace(reader.peek());
	}

	@Nullable
	public static Entity getEntityFromCollision(CollisionContext collisionContext) {
		return collisionContext instanceof EntityCollisionContext entityCollision
			? entityCollision.getEntity()
			: null;
	}

	public static boolean collisionHasEntity(CollisionContext collisionContext) {
		return getEntityFromCollision(collisionContext) != null;
	}

	public static HolderLookup.Provider getRegistries(ReloadableServerResources resources) {
		return ((ReloadableServerRegistriesAccessor.HolderAccessor) resources.fullRegistries()).getRegistries();
	}

	public static DataResult<CompoundTag> asCompoundTag(Tag tag) {
		return tag instanceof CompoundTag compoundTag
			? DataResult.success(compoundTag)
			: DataResult.error(() -> "Not a compound tag: " + tag.toString());
	}

	@Nullable
	public static BlockPos adjustSpawnLocationSafely(ServerPlayer serverPlayer, ServerLevel dimension, BlockPos blockPos) {
		return adjustSpawnLocationSafely((player, dim, pos) -> true, serverPlayer, dimension, blockPos);
	}

	@Nullable
	public static BlockPos adjustSpawnLocationSafely(TriPredicate<ServerPlayer, ServerLevel, BlockPos> safeLocationCondition, ServerPlayer serverPlayer, ServerLevel level, BlockPos pos) {

		MinecraftServer server = level.getServer();
		LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));

		int y = level.dimensionType().hasCeiling()
			? level.getChunkSource().getGenerator().getSpawnHeight(level)
			: chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() & 15, pos.getZ() & 15);

		int distanceToBorder = Mth.floor(level.getWorldBorder().getDistanceToBorder(pos.getX(), pos.getZ()));
		int radius = Mth.clamp(Math.max(0, server.getSpawnRadius(level)), 1, distanceToBorder);

		int diameter = radius * 2 + 1;
		int max = diameter * diameter;

		int coprime = ((ServerPlayerAccessor) serverPlayer).callGetCoprime(max);
		int offset = RandomSource.create().nextInt(max);

		int maxY = level.getLogicalHeight();
		int minY = level.getMinY();

		BlockPos.MutableBlockPos candidate = pos.mutable();
		int[] verticalOffsets = {0, 0};

		while (verticalOffsets[0] < maxY || verticalOffsets[1] > minY) {

			for (int step = 0; step < max; step++) {

				int q = (offset + coprime * step) % max;

				int xOffset = q % diameter;
				int zOffset = q / diameter;

				int x = pos.getX() + xOffset - radius;
				int z = pos.getZ() + zOffset - radius;

				for (int verticalOffset : verticalOffsets) {

					candidate.set(x, y + verticalOffset, z);

					for (int i = 0; i < 2; i++) {

						Vec3 safePos = DismountHelper.findSafeDismountLocation(serverPlayer.getType(), level, candidate, i == 0);

						if (safePos == null) {
							continue;
						}

						candidate.set(safePos.x(), safePos.y(), safePos.z());

						if (safeLocationCondition.test(serverPlayer, level, candidate)) {
							return candidate.immutable();
						}

					}

				}

			}

			verticalOffsets[0]++;
			verticalOffsets[1]--;

		}

		return null;

	}

	public static <E> void iterateList(List<E> list, BiIntegerConsumer<E> consumer, BooleanSupplier continueCondition) {

		if (list instanceof RandomAccess) {

			for (int i = 0; i < list.size() && continueCondition.getAsBoolean(); i++) {
				consumer.accept(i, list.get(i));
			}

		}

		else {

			ListIterator<E> listIterator = list.listIterator();

			while (listIterator.hasNext() && continueCondition.getAsBoolean()) {
				consumer.accept(listIterator.nextIndex(), listIterator.next());
			}

		}

	}

	public static <E> void iterateList(List<E> list, BiIntegerConsumer<E> consumer) {
		iterateList(list, consumer, BooleanSuppliers.ALWAYS_TRUE);
	}

	public static <E> void iterate(Iterable<E> iterable, BiIntegerConsumer<E> consumer, BooleanSupplier continueCondition) {

		if (iterable instanceof List<E> list) {
			iterateList(list, consumer, continueCondition);
		}

		else {

			Iterator<E> iterator = iterable.iterator();
			int index = 0;

			while (iterator.hasNext() && continueCondition.getAsBoolean()) {
				consumer.accept(index++, iterator.next());
			}

		}

	}

	public static <E> void iterate(Iterable<E> iterable, BiIntegerConsumer<E> consumer) {
		iterate(iterable, consumer, BooleanSuppliers.ALWAYS_TRUE);
	}

	public static <T, U extends T> Function<T, DataResult<U>> validateType(Class<U> typeClass, Supplier<String> errorSupplier) {
		return type -> {

			if (typeClass.isInstance(type)) {
				return DataResult.success(typeClass.cast(type));
			}

			else {
				return DataResult.error(errorSupplier);
			}

		};
	}

	public static void broadcastCustomToAll(Entity tracked, Supplier<CustomPacketPayload> customPacket) {
		broadcastCustom(null, tracked, customPacket);
	}

	public static void broadcastCustom(@Nullable Player except, Entity tracked, Supplier<CustomPacketPayload> customPacket) {

		Set<ServerPlayer> trackers = getTrackingOrEmpty(tracked);
		Supplier<CustomPacketPayload> memoizedPacket = Suppliers.memoize(customPacket::get);

		for (var tracker : trackers) {

			CustomPacketPayload actual = memoizedPacket.get();

			if (except != tracker && ServerPlayNetworking.canSend(tracker, actual.type())) {
				ServerPlayNetworking.send(tracker, actual);
			}

		}

	}

	public static void broadcastToAll(Entity tracked, Supplier<Packet<?>> packet) {
		broadcast(null, tracked, packet);
	}

	public static void broadcast(@Nullable Player except, Entity tracked, Supplier<Packet<?>> packet) {

		Set<ServerPlayer> trackers = getTrackingOrEmpty(tracked);
		Supplier<Packet<?>> memoizedPacket = Suppliers.memoize(packet::get);

		for (var tracker : trackers) {

			//noinspection ConstantValue
			if (tracker != except && tracker.connection != null) {
				tracker.connection.send(memoizedPacket.get());
			}

		}

	}

	public static <T> DataResult<T> handleResult(DataResult<T> result, Consumer<T> onSuccessOrPartial, Consumer<String> onPartial, Consumer<String> onError) {
		return handleResult(result, onSuccessOrPartial, Predicates.alwaysTrue(), onPartial, onError);
	}

	public static <T> DataResult<T> handleResult(DataResult<T> result, Consumer<T> onSuccessOrPartial, Predicate<T> partialFilter, Consumer<String> onPartial, Consumer<String> onError) {
		return result
			.ifSuccess(onSuccessOrPartial)
			.ifError(error -> error
				.resultOrPartial()
				.filter(partialFilter)
				.ifPresentOrElse(onSuccessOrPartial.andThen(t -> onPartial.accept(error.message())), () -> onError.accept(error.message())));
	}

	public static Map<ResourceLocation, JsonWithSource> collectJson(ResourceManager manager, JsonFileToIdConverter converter, DynamicOps<JsonElement> ops, Consumer<String> errorHandler) {
		return collectJson(manager, converter, ops, GSON, errorHandler);
	}

	public static Map<ResourceLocation, JsonWithSource> collectJson(ResourceManager manager, JsonFileToIdConverter converter, DynamicOps<JsonElement> ops, Gson gson, Consumer<String> errorHandler) {

		Map<ResourceLocation, JsonWithSource> result = new Object2ObjectOpenHashMap<>();
		Map<ResourceLocation, JsonFormat> history = new Object2ObjectOpenHashMap<>();

		converter.listMatchingResources(manager).forEach((fileId, resource) -> {

			String packId = resource.sourcePackId();
			ResourceLocation resourceId = converter.fileToId(fileId);

			try (BufferedReader resourceReader = resource.openAsReader()) {

				JsonFormat jsonFormat = converter.getFormat(fileId);
				GsonReader gsonReader = new GsonReader(JsonReader.create(resourceReader, jsonFormat));

				switch (gson.fromJson(gsonReader, JsonElement.class)) {
					case JsonElement asIs when isResourceConditionFulfilled(resourceId, asIs, converter.directory(), ops) -> {

						var prevFormat = history.putIfAbsent(resourceId, jsonFormat);

						if (prevFormat != null) {
							errorHandler.accept("JSON file \"" + fileId + "\" from pack [" + packId + "] has a duplicate with a different file extension! (prev. file extension: \"." + prevFormat.toString().toLowerCase(Locale.ROOT) + "\")");
						}

						else {
							result.putIfAbsent(resourceId, new JsonWithSource(asIs, packId));
						}

					}
					case JsonElement ignored -> {
						//  No-op since its resource conditions aren't fulfilled
					}
					case null ->
						errorHandler.accept("JSON file \"" + fileId + "\" is empty!");
					default -> {
						//  No-op since every JSON type should already be handled by the first case
					}
				}

			}

			catch (IOException e) {
				errorHandler.accept("Couldn't open JSON file \"" + fileId + "\" from pack [" + packId + "]: " + e);
			}

		});

		return result;

	}

	public static Map<ResourceLocation, List<JsonWithSource>> collectJsonStack(ResourceManager manager, JsonFileToIdConverter converter, DynamicOps<JsonElement> ops, Consumer<String> errorHandler) {
		return collectJsonStack(manager, converter, ops, GSON, errorHandler);
	}

	public static Map<ResourceLocation, List<JsonWithSource>> collectJsonStack(ResourceManager manager, JsonFileToIdConverter converter, DynamicOps<JsonElement> ops, Gson gson, Consumer<String> errorHandler) {

		Map<ResourceLocation, List<JsonWithSource>> result = new Object2ObjectOpenHashMap<>();
		Map<String, Map<ResourceLocation, JsonFormat>> history = new Object2ObjectOpenHashMap<>();

		converter.listMatchingResourceStacks(manager).forEach((fileId, resources) -> {

			for (var resource : resources) {

				String packId = resource.sourcePackId();
				ResourceLocation resourceId = converter.fileToId(fileId);

				try (BufferedReader resourceReader = resource.openAsReader()) {

					JsonFormat jsonFormat = converter.getFormat(fileId);
					GsonReader gsonReader = new GsonReader(JsonReader.create(resourceReader, jsonFormat));

					switch (gson.fromJson(gsonReader, JsonElement.class)) {
						case JsonElement asIs when isResourceConditionFulfilled(resourceId, asIs, converter.directory(), ops) -> {

							var prevFormat = history
								.computeIfAbsent(packId, k -> new Object2ObjectOpenHashMap<>())
								.putIfAbsent(resourceId, jsonFormat);

							if (prevFormat != null) {
								errorHandler.accept("JSON file \"" + fileId + "\" from pack [" + packId + "] has a duplicate with a different file extension! (prev. file extension: \"." + prevFormat.toString().toLowerCase(Locale.ROOT) + "\")");
							}

							else {
								result
									.computeIfAbsent(resourceId, k -> new ObjectArrayList<>())
									.add(new JsonWithSource(asIs, packId));
							}


						}
						case JsonElement ignored -> {
							//  No-op since its resource conditions aren't fulfilled
						}
						case null ->
							errorHandler.accept("JSON file \"" + fileId + "\" is empty!");
						default -> {
							//  No-op since every JSON type should already be handled by the first case
						}
					}

				}

				catch (IOException e) {
					errorHandler.accept("Couldn't open JSON file \"" + fileId + "\" from pack [" + packId + "]: " + e);
				}

			}

		});

		return result;

	}

	public static Optional<SlotAccess> createContainerSlotSafely(Container container, int slot) {

		if (slot >= 0 && slot < container.getContainerSize()) {
			return Optional.of(SlotAccess.forContainer(container, slot));
		}

		else {
			return Optional.empty();
		}

	}

	public static DataResult<Character> validateStringAsCharacter(String string) {
		return string.length() == 1
			? DataResult.success(string.charAt(0))
			: DataResult.error(() -> "'" + string + "' is an invalid symbol! (must be 1 character only)");
	}

	public static char unsafelyAssumeStringAsCharacter(String string) {
		return validateStringAsCharacter(string).getOrThrow();
	}

	public static <T> Iterable<T> emptyIterable() {
		//noinspection unchecked
		return (Iterable<T>) EMPTY_ITERABLE;
	}

	public static String join(Collection<String> strings, UnaryOperator<String> mapper, String endSeparator) {

		StringBuilder builder = new StringBuilder();
		Mutable<String> separator = new MutableObject<>("");

		iterate(
			strings,
			(index, string) -> {

				if (strings.size() > 1 && index >= strings.size() - 1) {
					separator.setValue(endSeparator);
				}

				else {
					separator.setValue(", ");
				}

				builder
					.append(separator)
					.append(mapper.apply(string));

			}
		);

		return builder.toString();

	}

	public static String allKeys(Collection<String> strings) {
		return join(strings, key -> "'" + key + "'", " and ");
	}

	public static String anyKeys(Collection<String> strings) {
		return join(strings, key -> "'" + key + "'", " or ");
	}

	public static <T> MapCodec.ResultFunction<T> validateWithInput(BiFunction<T, MapLike<?>, DataResult<T>> validator) {
		return new MapCodec.ResultFunction<>() {

			@Override
			public <T1> DataResult<T> apply(DynamicOps<T1> ops, MapLike<T1> input, DataResult<T> result) {
				return result.flatMap(t -> validator.apply(t, input));
			}

			@Override
			public <T1> RecordBuilder<T1> coApply(DynamicOps<T1> ops, T input, RecordBuilder<T1> prefix) {
				return prefix;
			}

		};
	}

}
