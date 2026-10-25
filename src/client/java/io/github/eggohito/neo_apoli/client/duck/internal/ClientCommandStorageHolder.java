package io.github.eggohito.neo_apoli.client.duck.internal;

import io.github.eggohito.neo_apoli.duck.internal.CommandStorageHolder;

public interface ClientCommandStorageHolder extends CommandStorageHolder {

	default void neo_apoli$clear() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

}
