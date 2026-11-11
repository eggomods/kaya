package io.github.eggohito.kaya.client.util.services.manager;

import io.github.eggohito.kaya.client.action.manager.ClientActionManager;
import io.github.eggohito.kaya.client.condition.manager.ClientConditionManager;
import io.github.eggohito.kaya.client.key.manager.ClientKeyStateManager;
import io.github.eggohito.kaya.client.power.manager.ClientPowerManager;
import io.github.eggohito.kaya.client.tag.manager.ClientNestedTagManager;
import io.github.eggohito.kaya.manager.Manager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.util.services.manager.Managers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

public final class KayaClientManagers implements Managers {

	private final Map<ResourceKey<?>, Manager> map = Map.of(
		KayaManagerKeys.ACTION, new ClientActionManager(),
		KayaManagerKeys.CONDITION, new ClientConditionManager(),
		KayaManagerKeys.POWER, new ClientPowerManager(),
		KayaManagerKeys.KEY_STATE, new ClientKeyStateManager(),
		KayaManagerKeys.NESTED_TAG, new ClientNestedTagManager()
	);

	@Override
	public @Nullable <M extends Manager> M get(ResourceKey<? extends M> key) {
		//noinspection unchecked
		return (M) this.map.get(key);
	}

	@Override
	public String toString() {
		return "KayaClientManagers[" + this.map.keySet().stream().map(ResourceKey::location).map(ResourceLocation::toString).collect(Collectors.joining(", ")) + "]";
	}

}
