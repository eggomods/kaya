package io.github.eggohito.kaya.power.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.config.AbstractJsonCodecConfig;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.resource.json.JsonWithSource;
import io.github.eggohito.kaya.util.CodecUtil;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.StreamCodecUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.parsers.json.JsonFormat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public record MultiplePower(ImmutableSet<PowerHolder<?>> subPowers, Properties properties) implements Power {

	public static final ResourceLocation ID = Kaya.id("multiple");

	private static final MapCodec<Power> DISALLOWED_MULTIPLE_CODEC = Type.CODEC.validate(MultiplePower::validateSubPowerType).dispatchMap(TYPE_KEY, Power::getType, Type::mapCodec);
	private static final MapCodec<PowerHolder<?>> DISALLOWED_MULTIPLE_HOLDER_CODEC = PowerHolder.mapCodec(DISALLOWED_MULTIPLE_CODEC);

	private static final MapCodec<Map<String, PowerHolder<?>>> MAP_SUB_POWERS_CODEC = MapCodec.assumeMapUnsafe(CodecUtil.filteredUnboundedMapCodec(Codec.STRING.validate(MultiplePower::validateSubPowerName), DISALLOWED_MULTIPLE_HOLDER_CODEC.codec(), Predicate.not(MultiplePower::isFieldIgnored)));
	private static final MapCodec<ImmutableSet<PowerHolder<?>>> SUB_POWERS_CODEC = MAP_SUB_POWERS_CODEC.xmap(
		map -> {

			ImmutableSet.Builder<PowerHolder<?>> builder = ImmutableSet.builder();
			map.values().forEach(builder::add);

			return builder.build();

		},
		set -> {

			Map<String, PowerHolder<?>> map = new Object2ObjectLinkedOpenHashMap<>();

			for (var holder : set) {

				if (holder.id() instanceof PowerIdentifier.SubImpl subId) {
					map.put(subId.name(), holder);
				}

			}

			return map;

		}
	);

	public static final MapCodec<MultiplePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		SUB_POWERS_CODEC.forGetter(MultiplePower::subPowers),
		Power.propertiesCodecBuilder(false)
	).apply(instance, MultiplePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MultiplePower> STREAM_CODEC = StreamCodec.composite(
		PowerHolder.STREAM_CODEC.apply(StreamCodecUtil.set()).map(ImmutableSet::copyOf, Function.identity()), MultiplePower::subPowers,
		Properties.STREAM_CODEC, MultiplePower::properties,
		MultiplePower::new
	);

	public MultiplePower {

		for (var subPower : subPowers) {
			validateSubPower(subPower.value()).getOrThrow();
		}

	}

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MULTIPLE;
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

		if (!(jsonWithSource.json() instanceof JsonObject powerJson) || !Type.CODEC.parse(ops, powerJson.get(TYPE_KEY)).mapOrElse(KayaPowerTypes.MULTIPLE::equals, error -> false)) {
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

	public static DataResult<Type<?>> validateSubPowerType(Power.Type<?> type) {

		if (type == KayaPowerTypes.MULTIPLE) {
			return DataResult.error(() -> "The '" + ID + "' power type is not allowed in sub-powers!");
		}

		else {
			return DataResult.success(type);
		}

	}

	public static DataResult<Power> validateSubPower(Power power) {
		return validateSubPowerType(power.getType()).map(ignored -> power);
	}

	public static final class Config extends AbstractJsonCodecConfig<Config> {

		public static final Config INSTANCE = new Config();

		public final ConfigEntry<List<Pattern>> ignoredFields = register("ignored_fields", makeDefaultIgnoredFields(), ExtraCodecs.PATTERN.listOf());

		Config() {
			super(FabricLoader.getInstance().getConfigDir().resolve("kaya/type/power/multiple.json5"), JsonFormat.JSON5);
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
