package io.github.eggohito.neo_apoli.condition.manager;

import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.util.manager.ContentManager;
import io.github.eggohito.neo_apoli.util.manager.NeoApoliManagers;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.NonExtendable
public interface ConditionManager extends ContentManager<ResourceLocation, Condition> {

	ResourceLocation ID = NeoApoli.id("manager/condition");

	Supplier<ConditionManager> DEFERRED_INSTANCE = NeoApoliManagers.deferredPhysicalSideSpecific(ConditionManager.class, ServerConditionManager::new);

	@ApiStatus.Internal
	void init();

	static ConditionManager getInstance() {
		return DEFERRED_INSTANCE.get();
	}

}
