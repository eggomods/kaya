package io.github.eggohito.neo_apoli.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import io.github.eggohito.neo_apoli.mixin.access.ResourceKeyAccessor;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.*;

public final class StreamCodecUtil {

	public static <B extends ByteBuf, E, C extends Collection<E>> StreamCodec<B, C> nonEmptyCollection(StreamCodec<B, C> codec) {
		return validated(codec, collection -> {

			if (collection.isEmpty()) {
				throw new IllegalStateException("Collection must have contents");
			}

			else {
				return collection;
			}

		});
	}

	public static <B extends ByteBuf, E> StreamCodec<B, E> validated(StreamCodec<B, E> codec, Function<E, E> validator) {
		return codec.map(validator, validator);
	}

	public static <B extends FriendlyByteBuf, E> StreamCodec<B, E> mapped(Supplier<BiMap<String, E>> supplier) {
		return new StreamCodec<>() {

			@Override
			public @NotNull E decode(B buf) {

				BiMap<String, E> mappedValues = supplier.get();
				E value = mappedValues.get(buf.readUtf());

				if (value != null) {
					return value;
				}

				else {
					throw new IllegalArgumentException("Expected value to be any of " + String.join(", ", mappedValues.keySet()));
				}

			}

			@Override
			public void encode(B buf, E value) {

				BiMap<String, E> mappedValues = supplier.get();
				String key = mappedValues.inverse().get(value);

				if (key != null) {
					buf.writeUtf(key);
				}

				else {
					throw new IllegalArgumentException("Value " + value + " is not associated with any keys!");
				}

			}

		};
	}

	public static <B extends FriendlyByteBuf, E> StreamCodec<B, E> mapped(BiMap<String, E> map) {
		return mapped(Suppliers.memoize(() -> map));
	}

	public static <B extends ByteBuf, E extends Enum<E>> StreamCodec<B, E> enumType(Class<E> enumClass) {

		ToIntFunction<E> toOrdinal = Enum::ordinal;
		IntFunction<E> fromOrdinal = ByIdMap.continuous(toOrdinal, enumClass.getEnumConstants(), ByIdMap.OutOfBoundsStrategy.CLAMP);

		return ByteBufCodecs.idMapper(fromOrdinal, toOrdinal).cast();

	}

	public static <B extends ByteBuf, A> StreamCodec<B, A> lazy(Supplier<StreamCodec<B, A>> delegate) {
		return lazy(delegate.toString(), Suppliers.memoize(delegate::get));
	}

	public static <B extends ByteBuf, A> StreamCodec<B, A> lazy(String name, Supplier<StreamCodec<B, A>> delegate) {
		return new StreamCodec<>() {

			@Override
			public @NotNull A decode(B buf) {
				return delegate.get().decode(buf);
			}

			@Override
			public void encode(B buf, A value) {
				delegate.get().encode(buf, value);
			}

			@Override
			public String toString() {
				return "LazyStreamCodec[" + name + "]";
			}

		};
	}

	public static <B extends ByteBuf, T> StreamCodec<B, T> unit(Supplier<T> supplier) {
		return StreamCodec.of((buf, value) -> supplier.get(), buf -> supplier.get());
	}

	public static <B extends ByteBuf, E> StreamCodec<B, WeightedList<E>> weightedList(StreamCodec<B, E> elementCodec) {
		return new StreamCodec<>() {

			@Override
			public @NotNull WeightedList<E> decode(B buf) {

				WeightedList.Builder<E> builder = WeightedList.builder();
				int size = buf.readInt();

				for (int i = 0; i < size; i++) {

					E element = elementCodec.decode(buf);
					int weight = buf.readInt();

					builder.add(element, weight);

				}

				return builder.build();

			}

			@Override
			public void encode(B buf, WeightedList<E> weightedList) {

				List<Weighted<E>> unwrapped = weightedList.unwrap();
				buf.writeInt(unwrapped.size());

				for (Weighted<E> weighted : unwrapped) {
					elementCodec.encode(buf, weighted.value());
					buf.writeInt(weighted.weight());
				}

			}

		};
	}

	public static <B extends ByteBuf, T> StreamCodec<B, ResourceKey<? extends Registry<T>>> registryKey() {
		return new StreamCodec<>() {

			@Override
			public @NotNull ResourceKey<? extends Registry<T>> decode(B buf) {

				ResourceLocation registry = ResourceLocation.STREAM_CODEC.decode(buf);
				ResourceLocation location = ResourceLocation.STREAM_CODEC.decode(buf);

				return ResourceKeyAccessor.callCreate(registry, location);

			}

			@Override
			public void encode(B buf, ResourceKey<? extends Registry<T>> input) {
				ResourceLocation.STREAM_CODEC.encode(buf, input.registry());
				ResourceLocation.STREAM_CODEC.encode(buf, input.location());
			}

		};
	}

	public static <B extends ByteBuf, E> StreamCodec.CodecOperation<B, E, Set<E>> set() {
		return streamCodec -> ByteBufCodecs.collection(ObjectOpenHashSet::new, streamCodec);
	}

	public static <B extends ByteBuf, T> StreamCodec.CodecOperation<B, T, T> handled(Consumer<Exception> onDecode, Consumer<Exception> onEncode) {
		return streamCodec -> new StreamCodec<>() {

			@Override
			public @NotNull T decode(B object) {

				try {
					return streamCodec.decode(object);
				} catch (Exception e) {
					onDecode.accept(e);
					throw e;
				}

			}

			@Override
			public void encode(B object, T object2) {

				try {
					streamCodec.encode(object, object2);
				} catch (Exception e) {
					onEncode.accept(e);
					throw e;
				}

			}

		};
	}

}
