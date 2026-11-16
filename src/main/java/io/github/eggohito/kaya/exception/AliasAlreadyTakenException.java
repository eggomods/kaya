package io.github.eggohito.kaya.exception;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AliasAlreadyTakenException extends RuntimeException {

	public AliasAlreadyTakenException(String message) {
		super(message);
	}

	public <T> AliasAlreadyTakenException(T from, T to, Supplier<T> existingSupplier) {
		this("Tried adding '" + from + "' as an alias for '" + to + "', but it's already an alias for '" + existingSupplier.get() + "'");
	}

	@Deprecated(forRemoval = true)
	public <T> AliasAlreadyTakenException(T from, T to, UnaryOperator<T> preExistingGetter) {
		this(from, to, () -> preExistingGetter.apply(from));
	}

}
