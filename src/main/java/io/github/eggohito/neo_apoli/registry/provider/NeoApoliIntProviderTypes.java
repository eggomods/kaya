package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliIntProviderTypes {

	public static final IntProvider.Type<AbsoluteIntProvider> ABSOLUTE = registerInternal("absolute", AbsoluteIntProvider.CODEC, AbsoluteIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ClampedIntProvider> CLAMPED = registerInternal("clamped", ClampedIntProvider.CODEC, ClampedIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<CompositeConditionalIntProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalIntProvider.CODEC, CompositeConditionalIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ConditionalIntProvider> CONDITIONAL = registerInternal("conditional", ConditionalIntProvider.CODEC, ConditionalIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ConstantIntProvider> CONSTANT = registerInternal("constant", ConstantIntProvider.CODEC, ConstantIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ContextIntProvider> CONTEXT = registerInternal("context", ContextIntProvider.CODEC, ContextIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<DifferenceIntProvider> DIFFERENCE = registerInternal("difference", DifferenceIntProvider.CODEC, DifferenceIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<FromFloatIntProvider> FROM_FLOAT = registerInternal("from_float", FromFloatIntProvider.CODEC, FromFloatIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<MaxIntProvider> MAX = registerInternal("max", MaxIntProvider.CODEC, MaxIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<MinIntProvider> MIN = registerInternal("min", MinIntProvider.CODEC, MinIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<NegateIntProvider> NEGATE = registerInternal("negate", NegateIntProvider.CODEC, NegateIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ProductIntProvider> PRODUCT = registerInternal("product", ProductIntProvider.CODEC, ProductIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<QuotientIntProvider> QUOTIENT = registerInternal("quotient", QuotientIntProvider.CODEC, QuotientIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<RandomBinomialIntProvider> RANDOM_BINOMIAL = registerInternal("random/binomial", RandomBinomialIntProvider.CODEC, RandomBinomialIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<RandomUniformIntProvider> RANDOM_UNIFORM = registerInternal("random/uniform", RandomUniformIntProvider.CODEC, RandomUniformIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<SumIntProvider> SUM = registerInternal("sum", SumIntProvider.CODEC, SumIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<WeightedIntProvider> WEIGHTED = registerInternal("weighted", WeightedIntProvider.CODEC, WeightedIntProvider.STREAM_CODEC);

	public static final IntProvider.Type<AdjacentBlocksIntProvider> ADJACENT_BLOCKS = registerInternal("adjacent_blocks", AdjacentBlocksIntProvider.CODEC, AdjacentBlocksIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<BlocksCollidingBoxIntProvider> BLOCKS_COLLIDING_BOX = registerInternal("blocks_colliding_box", BlocksCollidingBoxIntProvider.CODEC, BlocksCollidingBoxIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<BlocksIntersectingBoxIntProvider> BLOCKS_INTERSECTING_BOX = registerInternal("blocks_intersecting_box", BlocksIntersectingBoxIntProvider.CODEC, BlocksIntersectingBoxIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<CountInAreaIntProvider> COUNT_IN_AREA = registerInternal("count_in_area", CountInAreaIntProvider.CODEC, CountInAreaIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<EffectAmplifierIntProvider> EFFECT_AMPLIFIER = registerInternal("effect/amplifier", EffectAmplifierIntProvider.CODEC, EffectAmplifierIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<EntityActiveEffectsIntProvider> ENTITY_ACTIVE_EFFECTS = registerInternal("entity/active_effects", EntityActiveEffectsIntProvider.CODEC, EntityActiveEffectsIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<EquippedEnchantmentLevelIntProvider> EQUIPPED_ENCHANTMENT_LEVEL = registerInternal("equipped_enchantment_level", EquippedEnchantmentLevelIntProvider.CODEC, EquippedEnchantmentLevelIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ItemCountIntProvider> ITEM_COUNT = registerInternal("item/count", ItemCountIntProvider.CODEC, ItemCountIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ItemCountMaxIntProvider> ITEM_COUNT_MAX = registerInternal("item/count/max", ItemCountMaxIntProvider.CODEC, ItemCountMaxIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<ItemFuelIntProvider> ITEM_FUEL = registerInternal("item/fuel", ItemFuelIntProvider.CODEC, ItemFuelIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<KeyPressedTicksIntProvider> KEY_PRESSED_TICKS = registerInternal("key/pressed_ticks", KeyPressedTicksIntProvider.CODEC, KeyPressedTicksIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<KeyPressedTimeIntProvider> KEY_PRESSED_TIME = registerInternal("key/pressed_time", KeyPressedTimeIntProvider.CODEC, KeyPressedTimeIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<LightLevelIntProvider> LIGHT_LEVEL = registerInternal("light_level", LightLevelIntProvider.CODEC, LightLevelIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<NbtIntProvider> NBT = registerInternal("nbt", NbtIntProvider.CODEC, NbtIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<PlayerFoodIntProvider> PLAYER_FOOD = registerInternal("player/food", PlayerFoodIntProvider.CODEC, PlayerFoodIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<PowerCooldownRemainingTicksIntProvider> POWER_COOLDOWN_REMAINING_TICKS = registerInternal("power/cooldown/remaining_ticks", PowerCooldownRemainingTicksIntProvider.CODEC, PowerCooldownRemainingTicksIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<SlotIdIntProvider> SLOT_ID = registerInternal("slot_id", SlotIdIntProvider.CODEC, SlotIdIntProvider.STREAM_CODEC);
	public static final IntProvider.Type<TimeIntProvider> TIME = registerInternal("time", TimeIntProvider.CODEC, TimeIntProvider.STREAM_CODEC);

	public static void registerAll() {
		IntProvider.Type.ALIASES.addPathAlias("abs", ABSOLUTE);
		IntProvider.Type.ALIASES.addPathAlias("add", SUM);
		IntProvider.Type.ALIASES.addPathAlias("addition", SUM);
		IntProvider.Type.ALIASES.addPathAlias("div", QUOTIENT);
		IntProvider.Type.ALIASES.addPathAlias("divide", QUOTIENT);
		IntProvider.Type.ALIASES.addPathAlias("mul", PRODUCT);
		IntProvider.Type.ALIASES.addPathAlias("multiply", PRODUCT);
		IntProvider.Type.ALIASES.addPathAlias("sub", DIFFERENCE);
		IntProvider.Type.ALIASES.addPathAlias("diff", DIFFERENCE);
		IntProvider.Type.ALIASES.addPathAlias("subtract", DIFFERENCE);
		IntProvider.Type.ALIASES.addPathAlias("random", RANDOM_UNIFORM);
		IntProvider.Type.ALIASES.addPathAlias("rand", RANDOM_UNIFORM);
	}

	private static <P extends IntProvider> IntProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends IntProvider> IntProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.INT_PROVIDER_TYPE, id, new IntProvider.Type<>(mapCodec, streamCodec));
	}

}
