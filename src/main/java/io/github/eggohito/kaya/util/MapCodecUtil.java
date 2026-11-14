package io.github.eggohito.kaya.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class MapCodecUtil {

	public static <A> MapCodec<A> lazy(String name, Supplier<MapCodec<A>> delegate) {
		return MapCodec.recursive(name, ignored -> delegate.get());
	}

	public static <A> MapCodec<A> lazy(Supplier<MapCodec<A>> delegate) {
		return lazy(delegate.toString(), delegate);
	}

	public static <A> MapCodec<A> fail(Supplier<String> error) {
		return new MapCodec<>() {

			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return Stream.empty();
			}

			@Override
			public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
				return DataResult.error(error);
			}

			@Override
			public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				return prefix.withErrorsFrom(DataResult.error(error));
			}

		};
	}

	public static <T> MapCodec<T> either(MapCodec<T> primary, MapCodec<? extends T> secondary) {
		return Codec.mapEither(
			primary,
			secondary
		).xmap(
			Either::unwrap,
			Either::left
		);
	}

}
