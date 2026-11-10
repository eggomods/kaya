package io.github.eggohito.kaya.condition.manager;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.resource.DependencyList;
import io.github.eggohito.kaya.util.manager.ContentManager;
import io.github.eggohito.kaya.util.manager.KayaManagers;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.NonExtendable
public interface ConditionManager extends ContentManager<ResourceLocation, Condition> {

	ResourceLocation ID = Kaya.id("manager/condition");

	DependencyList DEPENDENCIES = new DependencyList(ID);

	Supplier<ConditionManager> DEFERRED_INSTANCE = KayaManagers.deferredPhysicalSideSpecific(ConditionManager.class, ServerConditionManager::new);

	@ApiStatus.Internal
	void init();

	static ConditionManager getInstance() {
		return DEFERRED_INSTANCE.get();
	}

}
