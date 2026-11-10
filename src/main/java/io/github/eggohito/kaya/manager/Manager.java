package io.github.eggohito.kaya.manager;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.mixin.access.ResourceKeyAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface Manager {

	final class Key {

		private static final ResourceLocation ROOT = Kaya.id("manager");

		public static <T> ResourceKey<T> create(String path) {
			return ResourceKeyAccessor.callCreate(ROOT, Kaya.id(path));
		}

	}

}
