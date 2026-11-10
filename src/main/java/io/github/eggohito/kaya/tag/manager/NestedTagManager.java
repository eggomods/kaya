package io.github.eggohito.kaya.tag.manager;

import io.github.eggohito.kaya.manager.Manager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.tag.NestedTag;
import io.github.eggohito.kaya.util.services.KayaServices;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface NestedTagManager extends Manager {

	<T> NestedTag<T> getOrCreate(ResourceKey<? extends Registry<T>> registryKey);

	@ApiStatus.Internal
	void init();

	static NestedTagManager getInstance() {
		return KayaServices.MANAGERS.getOrThrow(KayaManagerKeys.NESTED_TAG);
	}

}
