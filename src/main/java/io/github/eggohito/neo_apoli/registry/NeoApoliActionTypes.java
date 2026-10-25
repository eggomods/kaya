package io.github.eggohito.neo_apoli.registry;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.*;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliActionTypes {

	public static final Action.Type<CompositeConditionalAction> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalAction.CODEC, CompositeConditionalAction.STREAM_CODEC);
	public static final Action.Type<ConditionalAction> CONDITIONAL = registerInternal("conditional", ConditionalAction.CODEC, ConditionalAction.STREAM_CODEC);
	public static final Action.Type<LoopAction> LOOP = registerInternal("loop", LoopAction.CODEC, LoopAction.STREAM_CODEC);
	public static final Action.Type<NothingAction> NOTHING = registerInternal("nothing", NothingAction.CODEC, NothingAction.STREAM_CODEC);
	public static final Action.Type<ReferenceAction> REFERENCE = registerInternal("reference", ReferenceAction.CODEC, ReferenceAction.STREAM_CODEC);
	public static final Action.Type<SequenceAction> SEQUENCE = registerInternal("sequence", SequenceAction.CODEC, SequenceAction.STREAM_CODEC);
	public static final Action.Type<SideAction> SIDE = registerInternal("side", SideAction.CODEC, SideAction.STREAM_CODEC);
	public static final Action.Type<WeightedAction> WEIGHTED = registerInternal("weighted", WeightedAction.CODEC, WeightedAction.STREAM_CODEC);

	public static final Action.Type<AddVelocityAction> ADD_VELOCITY = registerInternal("add_velocity", AddVelocityAction.CODEC, AddVelocityAction.STREAM_CODEC);
	public static final Action.Type<ApplyEffectsAction> APPLY_EFFECTS = registerInternal("apply_effects", ApplyEffectsAction.CODEC, ApplyEffectsAction.STREAM_CODEC);
	public static final Action.Type<AreaOfEffectAction> AREA_OF_EFFECT = registerInternal("area_of_effect", AreaOfEffectAction.CODEC, AreaOfEffectAction.STREAM_CODEC);
	public static final Action.Type<BoneMealAction> BONE_MEAL = registerInternal("bone_meal", BoneMealAction.CODEC, BoneMealAction.STREAM_CODEC);
	public static final Action.Type<ClearPowersAction> CLEAR_POWERS = registerInternal("clear_powers", ClearPowersAction.CODEC, ClearPowersAction.STREAM_CODEC);
	public static final Action.Type<ConsumeItemAction> CONSUME_ITEM = registerInternal("consume_item", ConsumeItemAction.CODEC, ConsumeItemAction.STREAM_CODEC);
	public static final Action.Type<DamageEntityAction> DAMAGE_ENTITY = registerInternal("damage_entity", DamageEntityAction.CODEC, DamageEntityAction.STREAM_CODEC);
	public static final Action.Type<DamageItemAction> DAMAGE_ITEM = registerInternal("damage_item", DamageItemAction.CODEC, DamageItemAction.STREAM_CODEC);
	public static final Action.Type<DismountAction> DISMOUNT = registerInternal("dismount", DismountAction.CODEC, DismountAction.STREAM_CODEC);
	public static final Action.Type<EmitGameEventAction> EMIT_GAME_EVENT = registerInternal("emit_game_event", EmitGameEventAction.CODEC, EmitGameEventAction.STREAM_CODEC);
	public static final Action.Type<ExecuteCommandAction> EXECUTE_COMMAND = registerInternal("execute_command", ExecuteCommandAction.CODEC, ExecuteCommandAction.STREAM_CODEC);
	public static final Action.Type<ExhaustAction> EXHAUST = registerInternal("exhaust", ExhaustAction.CODEC, ExhaustAction.STREAM_CODEC);
	public static final Action.Type<ExplodeAction> EXPLODE = registerInternal("explode", ExplodeAction.CODEC, ExplodeAction.STREAM_CODEC);
	public static final Action.Type<ExtinguishEntityFireAction> EXTINGUISH_ENTITY_FIRE = registerInternal("extinguish_entity_fire", ExtinguishEntityFireAction.CODEC, ExtinguishEntityFireAction.STREAM_CODEC);
	public static final Action.Type<GainAirAction> GAIN_AIR = registerInternal("gain_air", GainAirAction.CODEC, GainAirAction.STREAM_CODEC);
	public static final Action.Type<GiveItemsAction> GIVE_ITEMS = registerInternal("give_items", GiveItemsAction.CODEC, GiveItemsAction.STREAM_CODEC);
	public static final Action.Type<GrantPowerAction> GRANT_POWER = registerInternal("grant_power", GrantPowerAction.CODEC, GrantPowerAction.STREAM_CODEC);
	public static final Action.Type<ModifyBlockStatePropertyAction> MODIFY_BLOCK_STATE_PROPERTY = registerInternal("modify_block_state_property", ModifyBlockStatePropertyAction.CODEC, ModifyBlockStatePropertyAction.STREAM_CODEC);
	public static final Action.Type<ModifyItemAction> MODIFY_ITEM = registerInternal("modify_item", ModifyItemAction.CODEC, ModifyItemAction.STREAM_CODEC);
	public static final Action.Type<MountAction> MOUNT = registerInternal("mount", MountAction.CODEC, MountAction.STREAM_CODEC);
	public static final Action.Type<PlaceBlockAction> PLACE_BLOCK = registerInternal("place_block", PlaceBlockAction.CODEC, PlaceBlockAction.STREAM_CODEC);
	public static final Action.Type<PlaySoundAction> PLAY_SOUND = registerInternal("play_sound", PlaySoundAction.CODEC, PlaySoundAction.STREAM_CODEC);
	public static final Action.Type<ProvideContextAction> PROVIDE_CONTEXT = registerInternal("provide_context", ProvideContextAction.CODEC, ProvideContextAction.STREAM_CODEC);
	public static final Action.Type<RandomChanceAction> RANDOM_CHANCE = registerInternal("random_chance", RandomChanceAction.CODEC, RandomChanceAction.STREAM_CODEC);
	public static final Action.Type<RemovePowerAction> REMOVE_POWER = registerInternal("remove_power", RemovePowerAction.CODEC, RemovePowerAction.STREAM_CODEC);
	public static final Action.Type<RevokeAllPowersAction> REVOKE_ALL_POWERS = registerInternal("revoke_all_powers", RevokeAllPowersAction.CODEC, RevokeAllPowersAction.STREAM_CODEC);
	public static final Action.Type<RevokePowerAction> REVOKE_POWER = registerInternal("revoke_power", RevokePowerAction.CODEC, RevokePowerAction.STREAM_CODEC);
	public static final Action.Type<SetEntityOnFireAction> SET_ENTITY_ON_FIRE = registerInternal("set_entity_on_fire", SetEntityOnFireAction.CODEC, SetEntityOnFireAction.STREAM_CODEC);
	public static final Action.Type<ShootEntityAction> SHOOT_ENTITY = registerInternal("shoot_entity", ShootEntityAction.CODEC, ShootEntityAction.STREAM_CODEC);
	public static final Action.Type<SpawnParticlesAction> SPAWN_PARTICLES = registerInternal("spawn_particles", SpawnParticlesAction.CODEC, SpawnParticlesAction.STREAM_CODEC);
	public static final Action.Type<SwingHandAction> SWING_HAND = registerInternal("swing_hand", SwingHandAction.CODEC, SwingHandAction.STREAM_CODEC);
	public static final Action.Type<TameAction> TAME = registerInternal("tame", TameAction.CODEC, TameAction.STREAM_CODEC);
	public static final Action.Type<TogglePowerAction> TOGGLE_POWER = registerInternal("toggle_power", TogglePowerAction.CODEC, TogglePowerAction.STREAM_CODEC);
	public static final Action.Type<TriggerPowerCooldownAction> TRIGGER_POWER_COOLDOWN = registerInternal("trigger_power_cooldown", TriggerPowerCooldownAction.CODEC, TriggerPowerCooldownAction.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <A extends Action> Action.Type<A> register(ResourceLocation id, MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {
		return Registry.register(NeoApoliRegistries.ACTION_TYPE, id, new Action.Type<>(mapCodec, streamCodec));
	}

	private static <A extends Action> Action.Type<A> registerInternal(String path, MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

}
