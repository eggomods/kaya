package io.github.eggohito.kaya.condition.manager;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.manager.ContentManager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.resource.DependencyList;
import io.github.eggohito.kaya.util.services.KayaServices;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ConditionManager extends ContentManager<ResourceLocation, Condition> {

	ResourceLocation ID = Kaya.id("manager/condition");

	DependencyList DEPENDENCIES = new DependencyList(ID);

	@ApiStatus.Internal
	void init();

	static ConditionManager getInstance() {
		return KayaServices.MANAGERS.getOrThrow(KayaManagerKeys.CONDITION);
	}

}
