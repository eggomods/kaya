package io.github.eggohito.kaya.duck;

import com.mojang.serialization.codecs.BaseMapCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface FilteredCodec<T> {

	default Predicate<T> kaya$getFilter() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void kaya$setFilter(@NotNull Predicate<T> filter) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@Nullable
	static <K, V> FilteredCodec<K> fromBaseMap(BaseMapCodec<K, V> codec) {

		if (codec instanceof FilteredCodec<?> filterable) {
			//noinspection unchecked
			return (FilteredCodec<K>) filterable;
		}

		else {
			return null;
		}

	}

}
