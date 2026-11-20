package io.github.eggohito.kaya.util;

public final class TriPredicates {

	private static final TriPredicate<?, ?, ?> ALWAYS_TRUE = (left, middle, right) -> true;

	@SuppressWarnings("unchecked")
	public static <L, M, R> TriPredicate<L, M, R> alwaysTrue() {
	    return (TriPredicate<L, M, R>) ALWAYS_TRUE;
	}

}
