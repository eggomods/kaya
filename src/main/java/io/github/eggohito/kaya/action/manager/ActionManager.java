package io.github.eggohito.kaya.action.manager;

import com.mojang.serialization.DataResult;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.action.ActionHolder;
import io.github.eggohito.kaya.manager.ContentAndTagManager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.resource.DependencyList;
import io.github.eggohito.kaya.util.services.KayaServices;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.NonExtendable
public interface ActionManager extends ContentAndTagManager<ResourceLocation, ActionHolder<?>> {

	ResourceLocation ID = Kaya.id("manager/action");

	DependencyList DEPENDENCIES = new DependencyList(ID);

	@Override
	default DataResult<ActionHolder<?>> getAsResult(ResourceLocation key) {
		return this.getAsResult(key, k -> "Unknown action: \"" + k + "\"");
	}

	@Override
	default DataResult<ResourceLocation> getKeyAsResult(ActionHolder<?> value) {
		return this.getKeyAsResult(value, v -> "Unregistered action holder: \"" + v + "\"");
	}

	@Override
	default DataResult<List<ActionHolder<?>>> getTagAsResult(ResourceLocation id) {
		return this.getTagAsResult(id, i -> "Unknown action tag: \"" + i + "\"");
	}

	default DataResult<ResourceLocation> getKeyAsResult(Action action) {

		for (var candidate : values()) {

			if (candidate.value() == action) {
				return DataResult.success(candidate.id());
			}

		}

		return DataResult.error(() -> "Unregistered action: " + action);

	}

	default ResourceLocation getKey(Action action) {
		return this.getKeyAsResult(action).getOrThrow();
	}

	@ApiStatus.Internal
	void init();

	static ActionManager getInstance() {
		return KayaServices.MANAGERS.getOrThrow(KayaManagerKeys.ACTION);
	}

}
