package io.github.eggohito.kaya.util.services.manager;

import io.github.eggohito.kaya.action.manager.ServerActionManager;
import io.github.eggohito.kaya.condition.manager.ServerConditionManager;
import io.github.eggohito.kaya.key.manager.ServerKeyStateManager;
import io.github.eggohito.kaya.manager.Manager;
import io.github.eggohito.kaya.power.manager.ServerPowerManager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.tag.manager.ServerNestedTagManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

public final class KayaServerManagers implements Managers {

	private final Map<ResourceKey<?>, Manager> map = Map.of(
		KayaManagerKeys.ACTION, new ServerActionManager(),
		KayaManagerKeys.CONDITION, new ServerConditionManager(),
		KayaManagerKeys.POWER, new ServerPowerManager(),
		KayaManagerKeys.KEY_STATE, new ServerKeyStateManager(),
		KayaManagerKeys.NESTED_TAG, new ServerNestedTagManager()
	);

	@Override
	public @Nullable <M extends Manager> M get(ResourceKey<? extends M> key) {
		//noinspection unchecked
		return (M) this.map.get(key);
	}

	@Override
	public String toString() {
		return "KayaServerManagers[" + this.map.keySet().stream().map(ResourceKey::location).map(ResourceLocation::toString).collect(Collectors.joining(", ")) + "]";
	}

}
