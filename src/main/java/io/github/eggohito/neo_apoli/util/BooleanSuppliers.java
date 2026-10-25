package io.github.eggohito.neo_apoli.util;

import java.util.function.BooleanSupplier;

public final class BooleanSuppliers {
	public static final BooleanSupplier ALWAYS_TRUE = () -> true;
	public static final BooleanSupplier ALWAYS_FALSE = () -> false;
}
