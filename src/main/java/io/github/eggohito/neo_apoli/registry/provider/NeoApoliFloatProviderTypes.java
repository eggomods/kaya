package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliFloatProviderTypes {

	public static final FloatProvider.Type<AbsoluteFloatProvider> ABSOLUTE = registerInternal("absolute", AbsoluteFloatProvider.CODEC, AbsoluteFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ClampedFloatProvider> CLAMPED = registerInternal("clamped", ClampedFloatProvider.CODEC, ClampedFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<CompositeConditionalFloatProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalFloatProvider.CODEC, CompositeConditionalFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ConditionalFloatProvider> CONDITIONAL = registerInternal("conditional", ConditionalFloatProvider.CODEC, ConditionalFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ConstantFloatProvider> CONSTANT = registerInternal("constant", ConstantFloatProvider.CODEC, ConstantFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ContextFloatProvider> CONTEXT = registerInternal("context", ContextFloatProvider.CODEC, ContextFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<DifferenceFloatProvider> DIFFERENCE = registerInternal("difference", DifferenceFloatProvider.CODEC, DifferenceFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<FromIntFloatProvider> FROM_INT = registerInternal("from_int", FromIntFloatProvider.CODEC, FromIntFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<LinearInterpolatedFloatProvider> LINEAR_INTERPOLATED = registerInternal("linear_interpolated", LinearInterpolatedFloatProvider.CODEC, LinearInterpolatedFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<MaxFloatProvider> MAX = registerInternal("max", MaxFloatProvider.CODEC, MaxFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<MinFloatProvider> MIN = registerInternal("min", MinFloatProvider.CODEC, MinFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<NegateFloatProvider> NEGATE = registerInternal("negate", NegateFloatProvider.CODEC, NegateFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<PowerFloatProvider> POWER = registerInternal("power", PowerFloatProvider.CODEC, PowerFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ProductFloatProvider> PRODUCT = registerInternal("product", ProductFloatProvider.CODEC, ProductFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<QuotientFloatProvider> QUOTIENT = registerInternal("quotient", QuotientFloatProvider.CODEC, QuotientFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<RandomUniformFloatProvider> RANDOM_UNIFORM = registerInternal("random/uniform", RandomUniformFloatProvider.CODEC, RandomUniformFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<RoundFloatProvider> ROUND = registerInternal("round", RoundFloatProvider.CODEC, RoundFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<SumFloatProvider> SUM = registerInternal("sum", SumFloatProvider.CODEC, SumFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<WeightedFloatProvider> WEIGHTED = registerInternal("weighted", WeightedFloatProvider.CODEC, WeightedFloatProvider.STREAM_CODEC);

	public static final FloatProvider.Type<BoxComponentFloatProvider> BOX_COMPONENT = registerInternal("box/component", BoxComponentFloatProvider.CODEC, BoxComponentFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<BoxSizeFloatProvider> BOX_SIZE = registerInternal("box/size", BoxSizeFloatProvider.CODEC, BoxSizeFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<BrightnessFloatProvider> BRIGHTNESS = registerInternal("brightness", BrightnessFloatProvider.CODEC, BrightnessFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<DistanceBetweenVectorsFloatProvider> DISTANCE_BETWEEN_VECTORS = registerInternal("distance_between_vectors", DistanceBetweenVectorsFloatProvider.CODEC, DistanceBetweenVectorsFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<EntityAttributeFloatProvider> ENTITY_ATTRIBUTE = registerInternal("entity/attribute", EntityAttributeFloatProvider.CODEC, EntityAttributeFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<EntityFluidHeightFloatProvider> ENTITY_FLUID_HEIGHT = registerInternal("entity/fluid_height", EntityFluidHeightFloatProvider.CODEC, EntityFluidHeightFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ItemAttributeFloatProvider> ITEM_ATTRIBUTE = registerInternal("item/attribute", ItemAttributeFloatProvider.CODEC, ItemAttributeFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<NbtFloatProvider> NBT = registerInternal("nbt", NbtFloatProvider.CODEC, NbtFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<PlayerSaturationFloatProvider> PLAYER_SATURATION = registerInternal("player/saturation", PlayerSaturationFloatProvider.CODEC, PlayerSaturationFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<PowerCooldownProgressFloatProvider> POWER_COOLDOWN_PROGRESS = registerInternal("power/cooldown/progress", PowerCooldownProgressFloatProvider.CODEC, PowerCooldownProgressFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<RainLevelFloatProvider> RAIN_LEVEL = registerInternal("rain_level", RainLevelFloatProvider.CODEC, RainLevelFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ResourceFloatProvider> RESOURCE = registerInternal("resource", ResourceFloatProvider.CODEC, ResourceFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<ThunderLevelFloatProvider> THUNDER_LEVEL = registerInternal("thunder_level", ThunderLevelFloatProvider.CODEC, ThunderLevelFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<VectorComponentFloatProvider> VECTOR_COMPONENT = registerInternal("vector/component", VectorComponentFloatProvider.CODEC, VectorComponentFloatProvider.STREAM_CODEC);
	public static final FloatProvider.Type<VectorLengthFloatProvider> VECTOR_LENGTH = registerInternal("vector/length", VectorLengthFloatProvider.CODEC, VectorLengthFloatProvider.STREAM_CODEC);

	public static void registerAll() {
		FloatProvider.Type.ALIASES.addPathAlias("abs", ABSOLUTE);
		FloatProvider.Type.ALIASES.addPathAlias("add", SUM);
		FloatProvider.Type.ALIASES.addPathAlias("addition", SUM);
		FloatProvider.Type.ALIASES.addPathAlias("div", QUOTIENT);
		FloatProvider.Type.ALIASES.addPathAlias("divide", QUOTIENT);
		FloatProvider.Type.ALIASES.addPathAlias("mul", PRODUCT);
		FloatProvider.Type.ALIASES.addPathAlias("multiply", PRODUCT);
		FloatProvider.Type.ALIASES.addPathAlias("sub", DIFFERENCE);
		FloatProvider.Type.ALIASES.addPathAlias("diff", DIFFERENCE);
		FloatProvider.Type.ALIASES.addPathAlias("subtract", DIFFERENCE);
		FloatProvider.Type.ALIASES.addPathAlias("random", RANDOM_UNIFORM);
		FloatProvider.Type.ALIASES.addPathAlias("rand", RANDOM_UNIFORM);
		FloatProvider.Type.ALIASES.addPathAlias("lerp", LINEAR_INTERPOLATED);
	}

	private static <P extends FloatProvider> FloatProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends FloatProvider> FloatProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.FLOAT_PROVIDER_TYPE, id, new FloatProvider.Type<>(mapCodec, streamCodec));
	}

}
