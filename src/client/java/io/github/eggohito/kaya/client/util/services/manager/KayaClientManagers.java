package io.github.eggohito.kaya.client.util.services.manager;

import io.github.eggohito.kaya.client.action.manager.ClientActionManager;
import io.github.eggohito.kaya.client.condition.manager.ClientConditionManager;
import io.github.eggohito.kaya.client.key.manager.ClientKeyStateManager;
import io.github.eggohito.kaya.client.power.manager.ClientPowerManager;
import io.github.eggohito.kaya.client.tag.manager.ClientNestedTagManager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.util.services.manager.AbstractManagers;

public final class KayaClientManagers extends AbstractManagers {

	public KayaClientManagers() {
		super(builder -> builder
			.put(KayaManagerKeys.ACTION, new ClientActionManager())
			.put(KayaManagerKeys.CONDITION, new ClientConditionManager())
			.put(KayaManagerKeys.POWER, new ClientPowerManager())
			.put(KayaManagerKeys.KEY_STATE, new ClientKeyStateManager())
			.put(KayaManagerKeys.NESTED_TAG, new ClientNestedTagManager())
		);
	}

}
