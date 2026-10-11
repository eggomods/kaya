package io.github.eggohito.neo_apoli.registry;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.hud.element.NumberBoundHudElement;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.*;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public final class NeoApoliPowerTypes {

	public static final Power.Type<CallbackBlockBreakPower> CALLBACK_BLOCK_BREAK = registerInternal(
		"callback/block/break",
		CallbackBlockBreakPower.CODEC,
		CallbackBlockBreakPower.STREAM_CODEC,
		params -> params
			.required(NeoApoliContextParameters.BROKEN_BLOCK)
			.optional(NeoApoliContextParameters.BROKEN_SIDE)
	);

	public static final Power.Type<CallbackBlockPlacePower> CALLBACK_BLOCK_PLACE = registerInternal(
		"callback/block/place",
		CallbackBlockPlacePower.CODEC,
		CallbackBlockPlacePower.STREAM_CODEC,
		builder -> builder
			.required(CallbackBlockPlacePower.PLACED_ON_BLOCK)
			.required(CallbackBlockPlacePower.PLACED_SIDE)
			.required(NeoApoliContextParameters.USED_ITEM)
			.required(NeoApoliContextParameters.USED_ITEM_SLOT)
	);

	public static final Power.Type<CallbackDamageDealtPower> CALLBACK_DAMAGE_DEALT = registerInternal(
		"callback/damage/dealt",
		CallbackDamageDealtPower.CODEC,
		CallbackDamageDealtPower.STREAM_CODEC,
		params -> params
			.required(NeoApoliContextParameters.DEALT_DAMAGE_SOURCE)
			.required(NeoApoliContextParameters.DEALT_DAMAGE_AMOUNT)
			.required(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
			.optional(NeoApoliContextParameters.DAMAGING_ENTITY)
			.optional(NeoApoliContextParameters.DIRECT_DAMAGING_ENTITY)
	);

	public static final Power.Type<CallbackPlayerRespawnedPower> CALLBACK_PLAYER_RESPAWNED = registerInternal(
		"callback/player/respawned",
		CallbackPlayerRespawnedPower.CODEC,
		CallbackPlayerRespawnedPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<CallbackPlayerWakeUpPower> CALLBACK_PLAYER_WAKE_UP = registerInternal(
		"callback/player/wake_up",
		CallbackPlayerWakeUpPower.CODEC,
		CallbackPlayerWakeUpPower.STREAM_CODEC,
		params -> params.required(CallbackPlayerWakeUpPower.SLEPT_ON_BLOCK)
	);

	public static final Power.Type<CallbackPowerAddedPower> CALLBACK_POWER_ADDED = registerInternal(
		"callback/power/added",
		CallbackPowerAddedPower.CODEC,
		CallbackPowerAddedPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<CallbackPowerGrantedPower> CALLBACK_POWER_GRANTED = registerInternal(
		"callback/power/granted",
		CallbackPowerGrantedPower.CODEC,
		CallbackPowerGrantedPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<CallbackPowerRemovedPower> CALLBACK_POWER_REMOVED = registerInternal(
		"callback/power/removed",
		CallbackPowerRemovedPower.CODEC,
		CallbackPowerRemovedPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<CallbackPowerRevokedPower> CALLBACK_POWER_REVOKED = registerInternal(
		"callback/power/revoked",
		CallbackPowerRevokedPower.CODEC,
		CallbackPowerRevokedPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<CallbackPowerTickPower> CALLBACK_POWER_TICK = registerInternal(
		"callback/power/tick",
		CallbackPowerTickPower.CODEC,
		CallbackPowerTickPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<CallbackProjectileLandPower> CALLBACK_PROJECTILE_LAND = registerInternal(
		"callback/projectile/land",
		CallbackProjectileLandPower.CODEC,
		CallbackProjectileLandPower.STREAM_CODEC,
		params -> params
			.required(CallbackProjectileLandPower.LANDED_ON_BLOCK)
			.required(NeoApoliContextParameters.THIS_ENTITY)
			.required(NeoApoliContextParameters.PROJECTILE_ENTITY)
			.optional(CallbackProjectileLandPower.LANDED_ON_SIDE)
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.optional(NeoApoliContextParameters.TARGET_ENTITY)
	);

	public static final Power.Type<CooldownStandalonePower> COOLDOWN = registerInternal(
		"cooldown",
		CooldownStandalonePower.CODEC,
		CooldownStandalonePower.STREAM_CODEC,
		params -> params
			.required(NumberBoundHudElement.CURRENT_VALUE)
			.required(NumberBoundHudElement.MAX_VALUE)
			.required(NumberBoundHudElement.MIN_VALUE)
	);

	public static final Power.Type<CraftingRecipePower> CRAFTING_RECIPE = registerInternal(
		"crafting_recipe",
		CraftingRecipePower.CODEC,
		CraftingRecipePower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<DummyPower> DUMMY = registerInternal(
		"dummy",
		DummyPower.CODEC,
		DummyPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<FloatResourcePower> RESOURCE_FLOAT = registerInternal(
		"resource/float",
		FloatResourcePower.CODEC,
		FloatResourcePower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<HudRenderPower> HUD_RENDER = registerInternal(
		"hud_render",
		HudRenderPower.CODEC,
		HudRenderPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<IntResourcePower> RESOURCE_INT = registerInternal(
		"resource/int",
		IntResourcePower.CODEC,
		IntResourcePower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<InventoryPower> INVENTORY = registerInternal(
		"inventory",
		InventoryPower.CODEC,
		InventoryPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyAirSpeedPower> MODIFY_AIR_SPEED = registerInternal(
		"modify/air/speed",
		ModifyAirSpeedPower.CODEC,
		ModifyAirSpeedPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyAttributePower> MODIFY_ATTRIBUTE = registerInternal(
		"modify/attribute",
		ModifyAttributePower.CODEC,
		ModifyAttributePower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyAttributeVanillaPower> MODIFY_ATTRIBUTE_VANILLA = registerInternal(
		"modify/attribute/vanilla",
		ModifyAttributeVanillaPower.CODEC,
		ModifyAttributeVanillaPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyBlockBreakSpeedPower> MODIFY_BLOCK_BREAK_SPEED = registerInternal(
		"modify/block/break_speed",
		ModifyBlockBreakSpeedPower.CODEC,
		ModifyBlockBreakSpeedPower.STREAM_CODEC,
		params -> params.required(NeoApoliContextParameters.MINING_BLOCK)
	);

	public static final Power.Type<ModifyBlockHarvestablePower> MODIFY_BLOCK_HARVESTABLE = registerInternal(
		"modify/block/harvestable",
		ModifyBlockHarvestablePower.CODEC,
		ModifyBlockHarvestablePower.STREAM_CODEC,
		params -> params.required(NeoApoliContextParameters.MINING_BLOCK)
	);

	public static final Power.Type<ModifyBlockSelectablePower> MODIFY_BLOCK_SELECTABLE = registerInternal(
		"modify/block/selectable",
		ModifyBlockSelectablePower.CODEC,
		ModifyBlockSelectablePower.STREAM_CODEC,
		params -> params.required(NeoApoliContextParameters.SELECTED_BLOCK)
	);

	public static final Power.Type<ModifyBlockUsePower> MODIFY_BLOCK_USE = registerInternal(
		"modify/block/use",
		ModifyBlockUsePower.CODEC,
		ModifyBlockUsePower.STREAM_CODEC,
		params -> params
			.required(ModifyBlockUsePower.USED_BLOCK)
			.required(ModifyBlockUsePower.USED_SIDE)
			.required(NeoApoliContextParameters.USED_ITEM_SLOT)
			.required(NeoApoliContextParameters.USED_ITEM)
	);

	public static final Power.Type<ModifyClimbingPower> MODIFY_CLIMBING = registerInternal(
		"modify/climbing",
		ModifyClimbingPower.CODEC,
		ModifyClimbingPower.STREAM_CODEC,
		params -> params.required(ModifyClimbingPower.CLIMBED_BLOCK)
	);

	public static final Power.Type<ModifyDamageDealtPower> MODIFY_DAMAGE_DEALT = registerInternal(
		"modify/damage/dealt",
		ModifyDamageDealtPower.CODEC,
		ModifyDamageDealtPower.STREAM_CODEC,
		keys -> keys
			.required(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
			.required(NeoApoliContextParameters.DEALT_DAMAGE_SOURCE)
			.required(NeoApoliContextParameters.DEALT_DAMAGE_AMOUNT)
			.optional(NeoApoliContextParameters.DAMAGING_ENTITY)
			.optional(NeoApoliContextParameters.DIRECT_DAMAGING_ENTITY)
	);

	public static final Power.Type<ModifyDamageInvulnerabilityPower> MODIFY_DAMAGE_INVULNERABILITY = registerInternal(
		"modify/damage/invulnerability",
		ModifyDamageInvulnerabilityPower.CODEC,
		ModifyDamageInvulnerabilityPower.STREAM_CODEC,
		keys -> keys
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
			.required(NeoApoliContextParameters.TAKEN_DAMAGE_SOURCE)
			.optional(NeoApoliContextParameters.DAMAGING_ENTITY)
			.optional(NeoApoliContextParameters.DIRECT_DAMAGING_ENTITY)
	);

	public static final Power.Type<ModifyDamageTakenPower> MODIFY_DAMAGE_TAKEN = registerInternal(
		"modify/damage/taken",
		ModifyDamageTakenPower.CODEC,
		ModifyDamageTakenPower.STREAM_CODEC,
		keys -> keys
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
			.required(NeoApoliContextParameters.TAKEN_DAMAGE_SOURCE)
			.required(NeoApoliContextParameters.TAKEN_DAMAGE_AMOUNT)
			.optional(NeoApoliContextParameters.DAMAGING_ENTITY)
			.optional(NeoApoliContextParameters.DIRECT_DAMAGING_ENTITY)
	);

	public static final Power.Type<ModifyEffectDurationPower> MODIFY_EFFECT_DURATION = registerInternal(
		"modify/effect/duration",
		ModifyEffectDurationPower.CODEC,
		ModifyEffectDurationPower.STREAM_CODEC,
		keys -> keys
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
			.required(NeoApoliContextParameters.APPLIED_EFFECT)
	);

	public static final Power.Type<ModifyEffectImmunityPower> MODIFY_EFFECT_IMMUNITY = registerInternal(
		"modify/effect/immunity",
		ModifyEffectImmunityPower.CODEC,
		ModifyEffectImmunityPower.STREAM_CODEC,
		keys -> keys
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
			.required(NeoApoliContextParameters.APPLIED_EFFECT)
	);

	public static final Power.Type<ModifyElytraFlightPower> MODIFY_ELYTRA_FLIGHT = registerInternal(
		"modify/elytra/flight",
		ModifyElytraFlightPower.CODEC,
		ModifyElytraFlightPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyElytraRenderPower> MODIFY_ELYTRA_RENDER = registerInternal(
		"modify/elytra/render",
		ModifyElytraRenderPower.CODEC,
		ModifyElytraRenderPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyEntityTypeTagPower> MODIFY_ENTITY_TYPE_TAG = registerInternal(
		"modify/entity/type_tag",
		ModifyEntityTypeTagPower.CODEC,
		ModifyEntityTypeTagPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyExhaustionPower> MODIFY_EXHAUSTION = registerInternal(
		"modify/exhaustion",
		ModifyExhaustionPower.CODEC,
		ModifyExhaustionPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyFallingPower> MODIFY_FALLING = registerInternal(
		"modify/falling",
		ModifyFallingPower.CODEC,
		ModifyFallingPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyGlowingOtherPower> MODIFY_GLOWING_OTHER = registerInternal(
		"modify/glowing/other",
		ModifyGlowingOtherPower.CODEC,
		ModifyGlowingOtherPower.STREAM_CODEC,
		keys -> keys
			.required(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
	);

	public static final Power.Type<ModifyGlowingSelfPower> MODIFY_GLOWING_SELF = registerInternal(
		"modify/glowing/self",
		ModifyGlowingSelfPower.CODEC,
		ModifyGlowingSelfPower.STREAM_CODEC,
		keys -> keys
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
	);

	public static final Power.Type<ModifyInvisibilityPower> MODIFY_INVISIBILITY = registerInternal(
		"modify/invisibility",
		ModifyInvisibilityPower.CODEC,
		ModifyInvisibilityPower.STREAM_CODEC,
		keys -> keys
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
	);

	public static final Power.Type<ModifyItemUsePower> MODIFY_ITEM_USE = registerInternal(
		"modify/item/use",
		ModifyItemUsePower.CODEC,
		ModifyItemUsePower.STREAM_CODEC,
		keys -> keys
			.required(NeoApoliContextParameters.USED_ITEM_SLOT)
			.required(NeoApoliContextParameters.USED_ITEM)
	);

	public static final Power.Type<ModifyItemWearablePower> MODIFY_ITEM_WEARABLE = registerInternal(
		"modify/item/wearable",
		ModifyItemWearablePower.CODEC,
		ModifyItemWearablePower.STREAM_CODEC,
		keys -> keys.required(ModifyItemWearablePower.WORN_ITEM)
	);

	public static final Power.Type<ModifyJumpPower> MODIFY_JUMP = registerInternal(
		"modify/jump",
		ModifyJumpPower.CODEC,
		ModifyJumpPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyModelColorOtherPower> MODIFY_MODEL_COLOR_OTHER = registerInternal(
		"modify/model/color/other",
		ModifyModelColorOtherPower.CODEC,
		ModifyModelColorOtherPower.STREAM_CODEC,
		keys -> keys
			.required(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
	);

	public static final Power.Type<ModifyModelColorSelfPower> MODIFY_MODEL_COLOR_SELF = registerInternal(
		"modify/model/color/self",
		ModifyModelColorSelfPower.CODEC,
		ModifyModelColorSelfPower.STREAM_CODEC,
		keys -> keys
			.optional(NeoApoliContextParameters.ACTOR_ENTITY)
			.required(NeoApoliContextParameters.TARGET_ENTITY)
	);

	public static final Power.Type<ModifyModelShakingPower> MODIFY_MODEL_SHAKING = registerInternal(
		"modify/model/shaking",
		ModifyModelShakingPower.CODEC,
		ModifyModelShakingPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyPlayerSpawnPower> MODIFY_PLAYER_SPAWN = registerInternal(
		"modify/player/spawn",
		ModifyPlayerSpawnPower.CODEC,
		ModifyPlayerSpawnPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<ModifyRecipeCraftingPower> MODIFY_RECIPE_CRAFTING = registerInternal(
		"modify/recipe/crafting",
		ModifyRecipeCraftingPower.CODEC,
		ModifyRecipeCraftingPower.STREAM_CODEC,
		keys -> keys
			.optional(ModifyRecipeCraftingPower.CRAFTING_BLOCK)
			.optional(ModifyRecipeCraftingPower.CRAFTED_ITEM_SLOT)
			.optional(ModifyRecipeCraftingPower.CRAFTED_ITEM)
	);

	public static final Power.Type<MultiplePower> MULTIPLE = registerInternal(
		"multiple",
		MultiplePower.CODEC,
		MultiplePower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<NbtPower> NBT = registerInternal(
		"nbt",
		NbtPower.CODEC,
		NbtPower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static final Power.Type<PhasingPower> PHASING = registerInternal(
		"phasing",
		PhasingPower.CODEC,
		PhasingPower.STREAM_CODEC,
		params -> params.required(PhasingPower.PHASED_BLOCK)
	);

	public static final Power.Type<ReplaceLootTablePower> REPLACE_LOOT_TABLE = registerInternal(
		"replace_loot_table",
		ReplaceLootTablePower.CODEC,
		ReplaceLootTablePower.STREAM_CODEC,
		builder -> builder
			.required(NeoApoliContextParameters.ACTOR_ENTITY)
			.optional(NeoApoliContextParameters.TARGET_ENTITY)
			.optional(NeoApoliContextParameters.BROKEN_BLOCK)
			.optional(ReplaceLootTablePower.TOOL_ITEM)
	);

	public static final Power.Type<TogglePower> TOGGLE = registerInternal(
		"toggle",
		TogglePower.CODEC,
		TogglePower.STREAM_CODEC,
		UnaryOperator.identity()
	);

	public static void registerAll() {

	}

	private static <P extends Power> Power.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec, UnaryOperator<ContextValidator.Parameters.Builder> parametersBuilder) {
		return register(NeoApoli.id(path), mapCodec, streamCodec, parametersBuilder);
	}

	public static <P extends Power> Power.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec, UnaryOperator<ContextValidator.Parameters.Builder> parametersBuilder) {

		ContextValidator.Parameters parameters = parametersBuilder.apply(new ContextValidator.Parameters.Builder())
			.required(NeoApoliContextParameters.THIS_ENTITY)
			.build();

		return Registry.register(NeoApoliRegistries.POWER_TYPE, id, new Power.Type<>(parameters, mapCodec, streamCodec));

	}

}
