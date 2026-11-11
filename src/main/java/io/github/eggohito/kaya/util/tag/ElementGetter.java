package io.github.eggohito.kaya.util.tag;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface ElementGetter<T> {

	@Nullable
	T element(ResourceLocation id, boolean required);

	@Nullable
	default T element(ResourceKey<T> key, boolean required) {
		return this.element(key.location(), required);
	}

}
