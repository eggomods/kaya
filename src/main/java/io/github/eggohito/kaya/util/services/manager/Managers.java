package io.github.eggohito.kaya.util.services.manager;

import io.github.eggohito.kaya.manager.Manager;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface Managers {

	@Nullable
	<M extends Manager> M get(ResourceKey<? extends M> key);

	default <M extends Manager> M getOrThrow(ResourceKey<? extends M> key) {
		return Objects.requireNonNull(this.get(key), "Unknown manager: \"" + key.location() + "\"");
	}

}
