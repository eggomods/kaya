package io.github.eggohito.kaya.util.services.manager;

import io.github.eggohito.kaya.action.manager.ServerActionManager;
import io.github.eggohito.kaya.condition.manager.ServerConditionManager;
import io.github.eggohito.kaya.key.manager.ServerKeyStateManager;
import io.github.eggohito.kaya.power.manager.ServerPowerManager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.tag.manager.ServerNestedTagManager;

public final class KayaServerManagers extends AbstractManagers {

	public KayaServerManagers() {
		super(builder -> builder
			.put(KayaManagerKeys.ACTION, new ServerActionManager())
			.put(KayaManagerKeys.CONDITION, new ServerConditionManager())
			.put(KayaManagerKeys.POWER, new ServerPowerManager())
			.put(KayaManagerKeys.KEY_STATE, new ServerKeyStateManager())
			.put(KayaManagerKeys.NESTED_TAG, new ServerNestedTagManager())
		);
	}

}
