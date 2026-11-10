package io.github.eggohito.kaya.registry.manager;

import io.github.eggohito.kaya.action.manager.ActionManager;
import io.github.eggohito.kaya.condition.manager.ConditionManager;
import io.github.eggohito.kaya.key.manager.KeyStateManager;
import io.github.eggohito.kaya.manager.Manager;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.tag.manager.NestedTagManager;
import net.minecraft.resources.ResourceKey;

public final class KayaManagerKeys {

	public static final ResourceKey<ActionManager> ACTION = Manager.Key.create("action");
	public static final ResourceKey<ConditionManager> CONDITION = Manager.Key.create("condition");
	public static final ResourceKey<PowerManager> POWER = Manager.Key.create("power");
	public static final ResourceKey<KeyStateManager> KEY_STATE = Manager.Key.create("key_state");
	public static final ResourceKey<NestedTagManager> NESTED_TAG = Manager.Key.create("nested_tag");

}
