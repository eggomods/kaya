package io.github.eggohito.kaya.util;

import java.util.Optional;
import java.util.function.Function;

public final class OptionalUtil {

	public static <T, U> Function<T, Optional<U>> flatCheckAndCast(Class<U> typeClass) {
		return t -> typeClass.isInstance(t)
			? Optional.of(typeClass.cast(t))
			: Optional.empty();
	}

}
