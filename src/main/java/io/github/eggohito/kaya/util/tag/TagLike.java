package io.github.eggohito.kaya.util.tag;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import org.apache.commons.lang3.function.Consumers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TagLike<E> implements ContextValidatable {

	private static final TagLike<?> EMPTY = new TagLike<>(ImmutableList.of(), () -> DataResult.success(ImmutableList.of()));

	private final ImmutableList<TagEntry> entries;
	private final Supplier<DataResult<ImmutableList<E>>> elementsSupplier;

	private TagLike(ImmutableList<TagEntry> entries, Supplier<DataResult<ImmutableList<E>>> elementsSupplier) {
		this.entries = entries;
		this.elementsSupplier = Suppliers.memoize(elementsSupplier::get);
	}

	@Override
	public void validate(ContextValidator validator) {
		this.getOrCreateElements(validator::reportProblem);
	}

	public ImmutableList<TagEntry> entries() {
		return entries;
	}

	public ImmutableList<E> elements() {
		return this.getOrCreateElements();
	}

	public boolean contains(E e) {
		return this.getOrCreateElements().contains(e);
	}

	public boolean isEmpty() {
		return this.getOrCreateElements().isEmpty();
	}

	private ImmutableList<E> getOrCreateElements(Consumer<String> onPartial) {
		return elementsSupplier.get().resultOrPartial(onPartial).orElse(ImmutableList.of());
	}

	private ImmutableList<E> getOrCreateElements() {
		return this.getOrCreateElements(Consumers.nop());
	}

	@SuppressWarnings("unchecked")
	public static <T> TagLike<T> empty() {
		return (TagLike<T>) EMPTY;
	}

	public static <T> TagLike<T> of(List<TagEntry> entries, TagLookup<T> lookup) {

		if (entries.isEmpty()) {
			return empty();
		}

		ImmutableList<TagEntry> copy = ImmutableList.copyOf(entries);
		return new TagLike<>(copy, () -> lookup.resolve(copy));

	}

	public static <T> TagLike<T> fromRegistry(List<TagEntry> entries, HolderLookup.RegistryLookup<T> registry) {
		return of(entries, TagLookup.fromRegistry(registry));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, TagLike<T>> registryStreamCodec(HolderLookup.RegistryLookup<T> registry) {
		return streamCodec(TagLookup.fromRegistry(registry));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, TagLike<T>> streamCodec(TagLookup<T> lookup) {
		return KayaStreamCodecs.TAG_ENTRIES.map(tagEntries -> of(tagEntries, lookup), TagLike::entries).cast();
	}

	public static <T> Codec<TagLike<T>> registryCodec(HolderLookup.RegistryLookup<T> registry) {
		return codec(TagLookup.fromRegistry(registry));
	}

	public static <T> Codec<TagLike<T>> registryCodec(ResourceKey<? extends Registry<T>> registryKey) {
		return new Codec<>() {

			@Override
			public <I> DataResult<Pair<TagLike<T>, I>> decode(DynamicOps<I> ops, I input) {

				if (ops instanceof RegistryOps<I> registryOps) {

					TagLookup<T> lookup = CodecUtil.getRegistry(registryOps, registryKey)
						.map(TagLookup::fromRegistry)
						.orElse(null);

					if (lookup == null) {
						return DataResult.error(() -> "Unknown registry: \"" + registryKey.location() + "\"");
					}

					else {
						return KayaCodecs.COMPACT_TAG_ENTRIES.decode(ops, input)
							.map(pair -> pair
								.mapFirst(entries -> of(entries, lookup)));
					}

				}

				else {
					return DataResult.error(() -> "Tag-likes require a registry ops!");
				}

			}

			@Override
			public <O> DataResult<O> encode(TagLike<T> input, DynamicOps<O> ops, O prefix) {
				return KayaCodecs.COMPACT_TAG_ENTRIES.encode(input.entries(), ops, prefix);
			}

		};
	}

	public static <T> Codec<TagLike<T>> codec(TagLookup<T> lookup) {
		return KayaCodecs.COMPACT_TAG_ENTRIES.xmap(entries -> of(entries, lookup), TagLike::entries);
	}

	public static final class Builder<T> {

		private final List<TagEntry> entries;
		private final TagLookup<T> lookup;

		Builder(List<TagEntry> entries, TagLookup<T> lookup) {
			this.entries = entries;
			this.lookup = lookup;
		}

		public Builder(TagLookup<T> lookup) {
			this(new ObjectArrayList<>(), lookup);
		}

		public Builder(HolderLookup.RegistryLookup<T> registry) {
			this(TagLookup.fromRegistry(registry));
		}

		public Builder<T> add(TagEntry entry) {
			this.entries.add(entry);
			return this;
		}

		public Builder<T> addAll(Collection<TagEntry> entries) {
			this.entries.addAll(entries);
			return this;
		}

		public Builder<T> addAll(TagEntry... entries) {
			return this.addAll(Arrays.asList(entries));
		}

		public TagLike<T> build() {
			return of(entries, lookup);
		}

	}

}
