package io.github.eggohito.neo_apoli.util.tag;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

	public static <T> TagLike<T> of(List<TagEntry> entries, TagEntry.Lookup<T> lookup) {
		ImmutableList<TagEntry> copy = ImmutableList.copyOf(entries);
		return new TagLike<>(copy, () -> toElementsWithPartial(copy, lookup));
	}

	public static <T> TagLike<T> ofRegistry(List<TagEntry> entries, HolderLookup.RegistryLookup<T> registry) {
		return of(entries, lookupFromRegistry(registry));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, TagLike<T>> registryStreamCodec(HolderLookup.RegistryLookup<T> registryLookup) {
		return streamCodec(lookupFromRegistry(registryLookup));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, TagLike<T>> streamCodec(TagEntry.Lookup<T> lookup) {
		return NeoApoliStreamCodecs.TAG_ENTRIES.map(tagEntries -> of(tagEntries, lookup), tag -> tag.entries).cast();
	}

	public static <T> Codec<TagLike<T>> registryCodec(HolderLookup.RegistryLookup<T> registry) {
		return codec(lookupFromRegistry(registry));
	}

	public static <T> Codec<TagLike<T>> registryCodec(ResourceKey<? extends Registry<T>> registryKey) {
		return new Codec<>() {

			@Override
			public <I> DataResult<Pair<TagLike<T>, I>> decode(DynamicOps<I> ops, I input) {

				if (ops instanceof RegistryOps<I> registryOps) {

					TagEntry.Lookup<T> lookup = CodecUtil.getRegistry(registryOps, registryKey)
						.map(TagLike::lookupFromRegistry)
						.orElse(null);

					if (lookup == null) {
						return DataResult.error(() -> "Unknown registry: \"" + registryKey.location() + "\"");
					}

					else {
						return NeoApoliCodecs.TAG_ENTRIES.decode(ops, input)
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
				return NeoApoliCodecs.TAG_ENTRIES.encode(input.entries, ops, prefix);
			}

		};
	}

	public static <T> Codec<TagLike<T>> codec(TagEntry.Lookup<T> lookup) {
		return NeoApoliCodecs.TAG_ENTRIES.xmap(entries -> of(entries, lookup), tag -> tag.entries);
	}

	private static <T> DataResult<ImmutableList<T>> toElementsWithPartial(List<TagEntry> entries, TagEntry.Lookup<T> lookup) {

		ImmutableList.Builder<T> foundBuilder = ImmutableList.builder();
		List<TagEntry> missing = new ObjectArrayList<>();

		for (var entry : entries) {

			if (!entry.build(lookup, foundBuilder::add)) {
				missing.add(entry);
			}

		}

		ImmutableList<T> found = foundBuilder.build();
		DataResult<ImmutableList<T>> result = DataResult.success(found);

		if (!missing.isEmpty()) {
			result = DataResult.error(() -> lookup.toString() + " is missing the following references: [" + missing.stream().map(Objects::toString).map(str -> "\"" + str + "\"").collect(Collectors.joining(", ")) + "]");
		}

		return result.setPartial(found);

	}

	private static <E> TagEntry.Lookup<E> lookupFromRegistry(HolderLookup.RegistryLookup<E> registry) {

		//noinspection unchecked
		ResourceKey<? extends Registry<E>> registryKey = (ResourceKey<? extends Registry<E>>) registry.key();

		return new TagEntry.Lookup<>() {

			@Override
			public @Nullable E element(ResourceLocation id, boolean required) {
				return registry.get(ResourceKey.create(registryKey, id))
					.map(Holder.Reference::value)
					.orElse(null);
			}

			@Override
			public @Nullable Collection<E> tag(ResourceLocation id) {
				return registry.get(TagKey.create(registryKey, id))
					.map(this::unwrapHolderSet)
					.orElse(null);
			}

			@Override
			public String toString() {
				return "Registry \"" + registryKey.location() + "\"";
			}

			private List<E> unwrapHolderSet(HolderSet<E> holderSet) {
				return holderSet.stream()
					.map(Holder::value)
					.toList();
			}

		};

	}

	public static final class Builder<T> {

		private final List<TagEntry> entries;
		private final TagEntry.Lookup<T> lookup;

		Builder(List<TagEntry> entries, TagEntry.Lookup<T> lookup) {
			this.entries = entries;
			this.lookup = lookup;
		}

		public Builder(TagEntry.Lookup<T> lookup) {
			this(new ObjectArrayList<>(), lookup);
		}

		public Builder(HolderLookup.RegistryLookup<T> registry) {
			this(lookupFromRegistry(registry));
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
