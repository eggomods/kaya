package io.github.eggohito.kaya.duck.internal;

import net.minecraft.server.MinecraftServer;

public interface CommandStorageExtension {

	default boolean kaya$hasChanged() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void kaya$sync(MinecraftServer server, boolean onlyWhenChanged) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

}
