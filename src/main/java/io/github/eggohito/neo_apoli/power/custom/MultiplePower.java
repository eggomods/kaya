package io.github.eggohito.neo_apoli.power.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.config.AbstractJsonCodecConfig;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.PowerHolder;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.resource.json.JsonWithSource;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.parsers.json.JsonFormat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public record MultiplePower(ImmutableSet<PowerHolder<?>> subPowers) implements Power {

	public static final ResourceLocation ID = NeoApoli.id("multiple");

	private static final MapCodec<Power> DISALLOWED_MULTIPLE_DISPATCH_CODEC = Type.CODEC.validate(MultiplePower::disallowRecursiveMultiple).dispatchMap(TYPE_KEY, Power::getType, Type::mapCodec);
	private static final MapCodec<PowerHolder<?>> DISALLOWED_MULTIPLE_MAP_CODEC = PowerHolder.mapCodec(DISALLOWED_MULTIPLE_DISPATCH_CODEC);

	private static final MapCodec<ImmutableSet<PowerHolder<?>>> SUB_POWERS_CODEC = new MapCodec<>() {

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.empty();
		}

		@Override
		public <I> DataResult<ImmutableSet<PowerHolder<?>>> decode(DynamicOps<I> ops, MapLike<I> mapInput) {

			Set<PowerHolder<?>> succeeded = new ObjectOpenHashSet<>();
			DataResult<Unit> result = mapInput.entries().reduce(
				DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
				(identity, keyAndValue) -> {

					DataResult<String> keyResult = Codec.STRING.parse(ops, keyAndValue.getFirst());
					DataResult<String> subPowerNameResult = keyResult.flatMap(MultiplePower::validateSubPowerName);

					if (keyResult.mapOrElse(MultiplePower::isFieldIgnored, error -> false)) {
						return identity;
					}

					if (subPowerNameResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, subPowerNameResult);
					}

					String subPowerName = subPowerNameResult.getOrThrow();
					DataResult<MapLike<I>> valueResult = ops.getMap(keyAndValue.getSecond()).mapError(error -> "Sub-power \"" + subPowerName + "\" errored: " + error);

					if (valueResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, valueResult);
					}

					MapLike<I> value = valueResult.getOrThrow();
					DataResult<PowerHolder<?>> subPowerResult = DISALLOWED_MULTIPLE_MAP_CODEC.decode(ops, value).mapError(error -> "Sub-power \"" + subPowerName + "\" errored: " + error);

					if (subPowerResult.isSuccess() && !succeeded.add(subPowerResult.getOrThrow())) {
						return identity.apply2stable((unit, o) -> unit, DataResult.error(() -> "Duplicate key: \"" + subPowerName + "\""));
					}

					return identity.apply2stable((unit, o) -> unit, subPowerResult);

				},
				(r1, r2) ->
					r1.apply2stable((u1, u2) -> u1, r2)
			);

			ImmutableSet<PowerHolder<?>> elements = ImmutableSet.copyOf(succeeded);
			return result.map(u -> elements).setPartial(elements);

		}

		@Override
		public <O> RecordBuilder<O> encode(ImmutableSet<PowerHolder<?>> entries, DynamicOps<O> ops, RecordBuilder<O> prefix) {

			for (var entry : entries) {
				prefix.add(entry.id().toString(), PowerHolder.CODEC.encodeStart(ops, entry));
			}

			return prefix;

		}

	};

	private static final StreamCodec<RegistryFriendlyByteBuf, ImmutableSet<PowerHolder<?>>> SUB_POWERS_STREAM_CODEC = new StreamCodec<>() {

		@Override
		public @NotNull ImmutableSet<PowerHolder<?>> decode(RegistryFriendlyByteBuf buf) {

			ImmutableSet.Builder<PowerHolder<?>> holders = ImmutableSet.builder();
			int size = buf.readInt();

			for (int i = 0; i < size; i++) {

				PowerHolder<?> powerHolder = PowerHolder.STREAM_CODEC.decode(buf);

				if (powerHolder.isSubPower()) {
					holders.add(powerHolder);
				}

				else {
					throw new IllegalArgumentException("Expected a sub-power, but got " + powerHolder.id().asDisplayString(false) + "!");
				}

			}

			return holders.build();

		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ImmutableSet<PowerHolder<?>> holders) {

			buf.writeInt(holders.size());

			for (var holder : holders) {
				PowerHolder.STREAM_CODEC.encode(buf, holder);
			}

		}

	};

	public static final MapCodec<MultiplePower> CODEC = SUB_POWERS_CODEC.xmap(
		MultiplePower::new,
		MultiplePower::subPowers
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, MultiplePower> STREAM_CODEC = SUB_POWERS_STREAM_CODEC.map(
		MultiplePower::new,
		MultiplePower::subPowers
	);

	public MultiplePower {

		for (var subPower : subPowers) {
			validateNonRecursiveMultiple(subPower);
		}

	}

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MULTIPLE;
	}

	@Override
	public Instance<?> createInstance() {
		return new Instance<>(this) {};
	}

	@Override
	public boolean canBePartiallyParsed() {
		return true;
	}

	/**
	 *  <p>Pre-processes the objects [whose keys aren't ignored] of the JSON of a power that uses the {@code multiple}
	 *  power type to prepare the objects to be parsed as "sub-powers".</p>
	 *
	 *  <p>In order for the sub-power to be recognized as a power, the ID of the super-power will have to be added
	 *  to the JSON object for the sub-power. The separator and the name of the key will be then used a suffix.</p>
	 */
	@ApiStatus.Internal
	public static void preProcessSubPowers(ResourceLocation id, JsonWithSource jsonWithSource, String directoryPath, DynamicOps<JsonElement> ops) {

		if (!(jsonWithSource.json() instanceof JsonObject powerJson) || !Type.CODEC.parse(ops, powerJson.get(TYPE_KEY)).mapOrElse(NeoApoliPowerTypes.MULTIPLE::equals, error -> false)) {
			return;
		}

		Map<String, JsonElement> powerJsonMap = powerJson.asMap();
		JsonObject copy = powerJson.deepCopy();

		powerJsonMap.clear();
		copy.asMap().forEach((key, value) -> {

			if (value instanceof JsonObject jsonObject && !isFieldIgnored(key) && MiscUtil.isResourceConditionFulfilled(id, jsonObject, directoryPath, ops)) {
				jsonObject.addProperty(PowerHolder.ID_KEY, id + String.valueOf(PowerIdentifier.SEPARATOR) + key);
			}

			powerJson.add(key, value);

		});

	}

	public static DataResult<String> validateSubPowerName(String name) {

		if (name.isEmpty()) {
			return DataResult.error(() -> "Empty sub-power names are not allowed!");
		}

		else if (ResourceLocation.isValidPath(name)) {
			return DataResult.success(name);
		}

		else {
			return DataResult.error(() -> "Non [a-z0-9/._-] character in sub-power name: \"" + name + "\"");
		}

	}

	public static boolean isFieldIgnored(String key) {

		for (var ignoredField : Config.INSTANCE.ignoredFields.get()) {

			if (ignoredField.matcher(key).matches()) {
				return true;
			}

		}

		return false;

	}

	public static DataResult<Type<?>> disallowRecursiveMultiple(Power.Type<?> type) {

		if (type == NeoApoliPowerTypes.MULTIPLE) {
			return DataResult.error(() -> "The '" + ID + "' power type is not allowed in sub-powers!");
		}

		else {
			return DataResult.success(type);
		}

	}

	public static PowerHolder<?> validateNonRecursiveMultiple(PowerHolder<?> holder) {
		disallowRecursiveMultiple(holder.value().getType()).getOrThrow();
		return holder;
	}

	public static final class Config extends AbstractJsonCodecConfig<Config> {

		public static final Config INSTANCE = new Config();

		public final ConfigEntry<List<Pattern>> ignoredFields = register("ignored_fields", makeDefaultIgnoredFields(), ExtraCodecs.PATTERN.listOf());

		Config() {
			super(FabricLoader.getInstance().getConfigDir().resolve("neo-apoli/type/power/multiple.json5"), JsonFormat.JSON5);
		}

		private static List<Pattern> makeDefaultIgnoredFields() {

			ImmutableList.Builder<Pattern> builder = ImmutableList.builder();
			PowerHolder.MAP_CODEC.keys(JavaOps.INSTANCE)
				.map(Object::toString)
				.filter(Predicate.not("value"::equals))
				.distinct()
				.map(Pattern::compile)
				.forEach(builder::add);

			builder.add(Pattern.compile("^\\$"));
			builder.add(Pattern.compile(ResourceConditions.CONDITIONS_KEY));

			return builder.build();

		}

	}

}
