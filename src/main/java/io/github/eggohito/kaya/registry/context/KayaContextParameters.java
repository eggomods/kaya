package io.github.eggohito.kaya.registry.context;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.parameter.*;

public final class KayaContextParameters {

	public static final EntityContextParameter COMMAND_ENTITY = new EntityContextParameter(Kaya.id("command/entity"));
	public static final Vec3ContextParameter COMMAND_POSITION = new  Vec3ContextParameter(Kaya.id("command/position"));

	public static final EntityContextParameter ACTOR_ENTITY = new EntityContextParameter(Kaya.id("actor_entity"));
	public static final EntityContextParameter PROJECTILE_ENTITY = new EntityContextParameter(Kaya.id("projectile_entity"));
	public static final EntityContextParameter TARGET_ENTITY = new EntityContextParameter(Kaya.id("target_entity"));
	public static final EntityContextParameter THIS_ENTITY = new EntityContextParameter(Kaya.id("this_entity"));

	public static final DamageSourceContextParameter DEALT_DAMAGE_SOURCE = new DamageSourceContextParameter(Kaya.id("dealt_damage/source"));
	public static final DamageSourceContextParameter TAKEN_DAMAGE_SOURCE = new DamageSourceContextParameter(Kaya.id("taken_damage/source"));
	public static final FloatContextParameter DEALT_DAMAGE_AMOUNT = new FloatContextParameter(Kaya.id("dealt_damage/amount"));
	public static final FloatContextParameter TAKEN_DAMAGE_AMOUNT = new FloatContextParameter(Kaya.id("taken_damage/amount"));

	public static final EntityContextParameter DAMAGING_ENTITY = new EntityContextParameter(Kaya.id("damaging_entity"));
	public static final EntityContextParameter DIRECT_DAMAGING_ENTITY = new EntityContextParameter(Kaya.id("direct_damaging_entity"));

	public static final BlockContextParameter BROKEN_BLOCK = new BlockContextParameter(Kaya.id("broken_block"));
	public static final DirectionContextParameter BROKEN_SIDE = new DirectionContextParameter(Kaya.id("broken_side"));

	public static final BlockContextParameter MINING_BLOCK = new BlockContextParameter(Kaya.id("mining_block"));
	public static final BlockContextParameter SELECTED_BLOCK = new BlockContextParameter(Kaya.id("selected_block"));

	public static final EffectContextParameter APPLIED_EFFECT = new EffectContextParameter(Kaya.id("applied_effect"));

	public static final ItemContextParameter ITEM_IN_CONTAINER = new ItemContextParameter(Kaya.id("item_in_container"));
	public static final SlotContextParameter ITEM_IN_CONTAINER_SLOT = new SlotContextParameter(Kaya.id("item_in_container_slot"));
	public static final ItemContextParameter USED_ITEM = new ItemContextParameter(Kaya.id("used_item"));
	public static final SlotContextParameter USED_ITEM_SLOT = new SlotContextParameter(Kaya.id("used_item_slot"));

	public static final IntContextParameter PLAYER_FOOD_LEVEL = new IntContextParameter(Kaya.id("player/food_level"));
	public static final FloatContextParameter PLAYER_SATURATION = new FloatContextParameter(Kaya.id("player/saturation"));

	public static void registerAll() {
		addPathAlias(KayaContextParameterTypes.ENTITY, "actor", ACTOR_ENTITY);
		addPathAlias(KayaContextParameterTypes.ENTITY, "projectile", PROJECTILE_ENTITY);
		addPathAlias(KayaContextParameterTypes.ENTITY, "target", TARGET_ENTITY);
		addPathAlias(KayaContextParameterTypes.ENTITY, "this", THIS_ENTITY);
	}

	public static <T extends ContextParameter.Type<P>, P extends ContextParameter<?>> void addPathAlias(T type, String from, P to) {
		type.aliases().getPaths().addAlias(from, to.name().getPath());
	}

}
