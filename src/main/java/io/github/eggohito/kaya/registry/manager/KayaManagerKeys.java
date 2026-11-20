package io.github.eggohito.kaya.registry.manager;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.manager.ActionManager;
import io.github.eggohito.kaya.condition.manager.ConditionManager;
import io.github.eggohito.kaya.key.manager.KeyStateManager;
import io.github.eggohito.kaya.manager.Manager;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.tag.manager.NestedTagManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class KayaManagerKeys {

	private static final ResourceLocation ROOT_ID = Kaya.id("manager");

	public static final ResourceKey<ActionManager> ACTION = create("action");
	public static final ResourceKey<ConditionManager> CONDITION = create("condition");
	public static final ResourceKey<PowerManager> POWER = create("power");
	public static final ResourceKey<KeyStateManager> KEY_STATE = create("key_state");
	public static final ResourceKey<NestedTagManager> NESTED_TAG = create("nested_tag");

	private static <M extends Manager> ResourceKey<M> create(String path) {
		return Manager.createKey(ROOT_ID, Kaya.id(path));
	}

}
