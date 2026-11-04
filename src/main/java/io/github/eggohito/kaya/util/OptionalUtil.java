package io.github.eggohito.kaya.util;

import java.util.Optional;
import java.util.function.Function;

public final class OptionalUtil {

	/**
	 *  Provides a function that checks and casts if the object of an optional is an instance of a specific class.
	 *  @param targetClass the class to check and cast the object to
	 *  @param <T> the original type of the optional
	 *  @param <U> the new type of the object
	 *  @return a populated {@link Optional} with its object type-casted to {@code <U>} if it's an instance of the
	 *  specified {@code targetClass}. Otherwise, {@link Optional#empty()}
	 */
	public static <T, U> Function<? super T, ? extends Optional<? extends U>> safeCast(Class<? extends U> targetClass) {
		return t -> targetClass.isInstance(t)
			? Optional.of(targetClass.cast(t))
			: Optional.empty();
	}

}
