package io.github.eggohito.kaya.manager;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.mixin.access.ResourceKeyAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface Manager {

	static <M extends Manager> ResourceKey<M> createKey(ResourceLocation root, ResourceLocation id) {
		return ResourceKeyAccessor.callCreate(root, id);
	}

	/**
	 *  Use {@link #createKey(ResourceLocation, ResourceLocation)} for creating manager keys instead
	 */
	@Deprecated(forRemoval = true)
	final class Key {

		private static final ResourceLocation ROOT = Kaya.id("manager");

		public static <T> ResourceKey<T> create(String path) {
			return ResourceKeyAccessor.callCreate(ROOT, Kaya.id(path));
		}

	}

}
