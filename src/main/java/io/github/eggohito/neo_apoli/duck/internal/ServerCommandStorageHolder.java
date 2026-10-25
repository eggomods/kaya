package io.github.eggohito.neo_apoli.duck.internal;

import net.minecraft.server.level.ServerPlayer;

public interface ServerCommandStorageHolder extends CommandStorageHolder {

	default void neo_apoli$sendAll(ServerPlayer recipient) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

}
