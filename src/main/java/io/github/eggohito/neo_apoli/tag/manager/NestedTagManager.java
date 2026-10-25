package io.github.eggohito.neo_apoli.tag.manager;

import io.github.eggohito.neo_apoli.tag.NestedTag;
import io.github.eggohito.neo_apoli.util.manager.NeoApoliManagers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.NonExtendable
public interface NestedTagManager {

	Supplier<NestedTagManager> DEFERRED_INSTANCE = NeoApoliManagers.deferredPhysicalSideSpecific(NestedTagManager.class, ServerNestedTagManager::new);

	<T> NestedTag<T> getOrCreate(ResourceKey<? extends Registry<T>> registryKey);

	@ApiStatus.Internal
	void init();

	static NestedTagManager getInstance() {
		return DEFERRED_INSTANCE.get();
	}

}
