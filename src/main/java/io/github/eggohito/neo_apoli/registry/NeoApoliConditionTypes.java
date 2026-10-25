package io.github.eggohito.neo_apoli.registry;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.*;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliConditionTypes {

	public static final Condition.Type<AllOfCondition> ALL_OF = registerInternal("all_of", AllOfCondition.CODEC, AllOfCondition.STREAM_CODEC);
	public static final Condition.Type<AnyOfCondition> ANY_OF = registerInternal("any_of", AnyOfCondition.CODEC, AnyOfCondition.STREAM_CODEC);
	public static final Condition.Type<CompareCondition> COMPARE = registerInternal("compare", CompareCondition.CODEC, CompareCondition.STREAM_CODEC);
	public static final Condition.Type<ConstantCondition> CONSTANT = registerInternal("constant", ConstantCondition.CODEC, ConstantCondition.STREAM_CODEC);
	public static final Condition.Type<DifficultyCondition> DIFFICULTY = registerInternal("difficulty", DifficultyCondition.CODEC, DifficultyCondition.STREAM_CODEC);
	public static final Condition.Type<DynamicCondition> DYNAMIC = registerInternal("dynamic", DynamicCondition.CODEC, DynamicCondition.STREAM_CODEC);
	public static final Condition.Type<InvertedCondition> INVERTED = registerInternal("inverted", InvertedCondition.CODEC, InvertedCondition.STREAM_CODEC);
	public static final Condition.Type<ParameterExistsCondition> PARAMETER_EXISTS = registerInternal("parameter_exists", ParameterExistsCondition.CODEC, ParameterExistsCondition.STREAM_CODEC);
	public static final Condition.Type<ReferenceCondition> REFERENCE = registerInternal("reference", ReferenceCondition.CODEC, ReferenceCondition.STREAM_CODEC);

	public static final Condition.Type<BlockStatePropertyCondition> BLOCK_STATE_PROPERTY = registerInternal("block_state_property", BlockStatePropertyCondition.CODEC, BlockStatePropertyCondition.STREAM_CODEC);
	public static final Condition.Type<CheckBlockTypeCondition> CHECK_BLOCK_TYPE = registerInternal("check_block_type", CheckBlockTypeCondition.CODEC, CheckBlockTypeCondition.STREAM_CODEC);
	public static final Condition.Type<CheckDamageTypeCondition> CHECK_DAMAGE_TYPE = registerInternal("check_damage_type", CheckDamageTypeCondition.CODEC, CheckDamageTypeCondition.STREAM_CODEC);
	public static final Condition.Type<CheckEffectTypeCondition> CHECK_EFFECT_TYPE = registerInternal("check_effect_type", CheckEffectTypeCondition.CODEC, CheckEffectTypeCondition.STREAM_CODEC);
	public static final Condition.Type<CheckEntityTypeCondition> CHECK_ENTITY_TYPE = registerInternal("check_entity_type", CheckEntityTypeCondition.CODEC, CheckEntityTypeCondition.STREAM_CODEC);
	public static final Condition.Type<CheckPrecipitationCondition> CHECK_PRECIPITATION = registerInternal("check_precipitation", CheckPrecipitationCondition.CODEC, CheckPrecipitationCondition.STREAM_CODEC);
	public static final Condition.Type<EntityHasActivePowerCondition> ENTITY_HAS_ACTIVE_POWER = registerInternal("entity_has_active_power", EntityHasActivePowerCondition.CODEC, EntityHasActivePowerCondition.STREAM_CODEC);
	public static final Condition.Type<EntityHasCorrectToolForBlockCondition> ENTITY_HAS_CORRECT_TOOL_FOR_BLOCK = registerInternal("entity_has_correct_tool_for_block", EntityHasCorrectToolForBlockCondition.CODEC, EntityHasCorrectToolForBlockCondition.STREAM_CODEC);
	public static final Condition.Type<EntityHasItemEquippedCondition> ENTITY_HAS_ITEM_EQUIPPED = registerInternal("entity_has_item_equipped", EntityHasItemEquippedCondition.CODEC, EntityHasItemEquippedCondition.STREAM_CODEC);
	public static final Condition.Type<EntityHasPowerCondition> ENTITY_HAS_POWER = registerInternal("entity_has_power", EntityHasPowerCondition.CODEC, EntityHasPowerCondition.STREAM_CODEC);
	public static final Condition.Type<EntityHasPressedKeyCondition> ENTITY_HAS_PRESSED_KEY = registerInternal("entity_has_pressed_key", EntityHasPressedKeyCondition.CODEC, EntityHasPressedKeyCondition.STREAM_CODEC);
	public static final Condition.Type<IsBlockEntityCondition> IS_BLOCK_ENTITY = registerInternal("is_block_entity", IsBlockEntityCondition.CODEC, IsBlockEntityCondition.STREAM_CODEC);
	public static final Condition.Type<IsBlockReplaceableCondition> IS_BLOCK_REPLACEABLE = registerInternal("is_block_replaceable", IsBlockReplaceableCondition.CODEC, IsBlockReplaceableCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntityClimbingCondition> IS_ENTITY_CLIMBING = registerInternal("is_entity_climbing", IsEntityClimbingCondition.CODEC, IsEntityClimbingCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntityFallFlyingCondition> IS_ENTITY_FALL_FLYING = registerInternal("is_entity_fall_flying", IsEntityFallFlyingCondition.CODEC, IsEntityFallFlyingCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntityHorizontallyCollidingCondition> IS_ENTITY_HORIZONTALLY_COLLIDING = registerInternal("is_entity_horizontally_colliding", IsEntityHorizontallyCollidingCondition.CODEC, IsEntityHorizontallyCollidingCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntityInvisibleCondition> IS_ENTITY_INVISIBLE = registerInternal("is_entity_invisible", IsEntityInvisibleCondition.CODEC, IsEntityInvisibleCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntityOnFireCondition> IS_ENTITY_ON_FIRE = registerInternal("is_entity_on_fire", IsEntityOnFireCondition.CODEC, IsEntityOnFireCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntityOwnedByOtherCondition> IS_ENTITY_OWNED_BY_OTHER = registerInternal("is_entity_owned_by_other", IsEntityOwnedByOtherCondition.CODEC, IsEntityOwnedByOtherCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntitySneakingCondition> IS_ENTITY_SNEAKING = registerInternal("is_entity_sneaking", IsEntitySneakingCondition.CODEC, IsEntitySneakingCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntitySprintingCondition> IS_ENTITY_SPRINTING = registerInternal("is_entity_sprinting", IsEntitySprintingCondition.CODEC, IsEntitySprintingCondition.STREAM_CODEC);
	public static final Condition.Type<IsEntitySteppingOnBlockCondition> IS_ENTITY_STEPPING_ON_BLOCK = registerInternal("is_entity_stepping_on_block", IsEntitySteppingOnBlockCondition.CODEC, IsEntitySteppingOnBlockCondition.STREAM_CODEC);
	public static final Condition.Type<IsExposedToSkyCondition> IS_EXPOSED_TO_SKY = registerInternal("is_exposed_to_sky", IsExposedToSkyCondition.CODEC, IsExposedToSkyCondition.STREAM_CODEC);
	public static final Condition.Type<IsSkyVisibleCondition> IS_SKY_VISIBLE = registerInternal("is_sky_visible", IsSkyVisibleCondition.CODEC, IsSkyVisibleCondition.STREAM_CODEC);
	public static final Condition.Type<ItemMatchesIngredientCondition> ITEM_MATCHES_INGREDIENT = registerInternal("item_matches_ingredient", ItemMatchesIngredientCondition.CODEC, ItemMatchesIngredientCondition.STREAM_CODEC);
	public static final Condition.Type<ItemMatchesPredicateCondition> ITEM_MATCHES_PREDICATE = registerInternal("item_matches_predicate", ItemMatchesPredicateCondition.CODEC, ItemMatchesPredicateCondition.STREAM_CODEC);
	public static final Condition.Type<MatchesBlockPatternCondition> MATCHES_BLOCK_PATTERN = registerInternal("matches_block_pattern", MatchesBlockPatternCondition.CODEC, MatchesBlockPatternCondition.STREAM_CODEC);
	public static final Condition.Type<ProvideContextCondition> PROVIDE_CONTEXT = registerInternal("provide_context", ProvideContextCondition.CODEC, ProvideContextCondition.STREAM_CODEC);

	public static void registerAll() {

	}

	private static <C extends Condition> Condition.Type<C> registerInternal(String path, MapCodec<C> mapCodec, StreamCodec<RegistryFriendlyByteBuf, C> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <C extends Condition> Condition.Type<C> register(ResourceLocation id, MapCodec<C> mapCodec, StreamCodec<RegistryFriendlyByteBuf, C> streamCodec) {
		return Registry.register(NeoApoliRegistries.CONDITION_TYPE, id, new Condition.Type<>(mapCodec, streamCodec));
	}

}
