package io.github.eggohito.kaya.util.tag;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.manager.ContentAndTagManager;
import io.github.eggohito.kaya.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.experimental.Accessors;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class TagLike<E> implements ContextValidatable {

	private static final TagLike<?> EMPTY = new TagLike<>(ImmutableList.of(), () -> DataResult.success(ImmutableList.of()));
	private static final File<?> EMPTY_FILE = new File.Inline<>(EMPTY);

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

	public static <T> TagLike<T> of(List<TagEntry> entries, Source<T> lookup) {

		if (entries.isEmpty()) {
			return empty();
		}

		ImmutableList<TagEntry> copy = ImmutableList.copyOf(entries);
		return new TagLike<>(copy, () -> lookup.resolve(copy));

	}

	public static <T> TagLike<T> fromRegistry(List<TagEntry> entries, HolderLookup.RegistryLookup<T> registry) {
		return of(entries, Source.fromRegistry(registry));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, TagLike<T>> registryStreamCodec(HolderLookup.RegistryLookup<T> registry) {
		return streamCodec(Source.fromRegistry(registry));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, TagLike<T>> streamCodec(Source<T> lookup) {
		return KayaStreamCodecs.TAG_ENTRIES.map(tagEntries -> of(tagEntries, lookup), TagLike::entries).cast();
	}

	public static <T> Codec<TagLike<T>> registryCodec(HolderLookup.RegistryLookup<T> registry) {
		return codec(Source.fromRegistry(registry));
	}

	public static <T> Codec<TagLike<T>> registryCodec(ResourceKey<? extends Registry<T>> registryKey) {
		return new Codec<>() {

			@Override
			public <I> DataResult<Pair<TagLike<T>, I>> decode(DynamicOps<I> ops, I input) {

				if (ops instanceof RegistryOps<I> registryOps) {

					Source<T> source = CodecUtil.getRegistry(registryOps, registryKey)
						.map(Source::fromRegistry)
						.orElse(null);

					if (source == null) {
						return DataResult.error(() -> "Unknown registry: \"" + registryKey.location() + "\"");
					}

					else {
						return KayaCodecs.COMPACT_TAG_ENTRIES.decode(ops, input)
							.map(pair -> pair
								.mapFirst(entries -> of(entries, source)));
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

	public static <T> Codec<TagLike<T>> codec(Source<T> lookup) {
		return KayaCodecs.COMPACT_TAG_ENTRIES.xmap(entries -> of(entries, lookup), TagLike::entries);
	}

	public static final class Builder<T> {

		private final List<TagEntry> entries;
		private final Source<T> lookup;

		Builder(List<TagEntry> entries, Source<T> lookup) {
			this.entries = entries;
			this.lookup = lookup;
		}

		public Builder(Source<T> lookup) {
			this(new ObjectArrayList<>(), lookup);
		}

		public Builder(HolderLookup.RegistryLookup<T> registry) {
			this(Source.fromRegistry(registry));
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

	public sealed interface File<E> extends ContextValidatable {

		File<E> withValues(TagLike<E> values);

		TagLike<E> values();

		boolean replace();

		@SuppressWarnings("unchecked")
		static <T> File<T> empty() {
			return (File<T>) EMPTY_FILE;
		}

		record Inline<E>(TagLike<E> values) implements File<E> {

			@Override
			public File<E> withValues(TagLike<E> values) {
				return new Inline<>(values);
			}

			@Override
			public boolean replace() {
				return false;
			}

			@Override
			public void validate(ContextValidator validator) {
				values().validate(validator);
			}

		}

		record Definition<E>(TagLike<E> values, boolean replace) implements File<E> {

			@Override
			public File<E> withValues(TagLike<E> values) {
				return new Definition<>(values, this.replace());
			}

			@Override
			public void validate(ContextValidator validator) {
				values().validate(validator.forChild(".values"));
			}

		}

	}

	@Accessors(fluent = true)
	public static class Source<E> implements TagEntry.Lookup<E>, ElementGetter<E>, TagGetter<E> {

		private final ElementGetter<E> elementGetter;
		private final TagGetter<E> tagGetter;

		@Getter
		private final Codec<TagLike<E>> codec;
		@Getter
		private final StreamCodec<ByteBuf, TagLike<E>> streamCodec;
		@Getter
		private final Codec<File<E>> fileCodec;

		private Source(ElementGetter<E> elementGetter, TagGetter<E> tagGetter) {

			this.elementGetter = elementGetter;
			this.tagGetter = tagGetter;

			Codec<TagLike<E>> tagCodec = TagLike.codec(this);
			Codec<File<E>> fullCodec = RecordCodecBuilder.create(instance -> instance.group(
				tagCodec.fieldOf("values").forGetter(File::values),
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(File::replace)
			).apply(instance, File.Definition::new));

			this.codec = tagCodec;
			this.streamCodec = TagLike.streamCodec(this);
			this.fileCodec = Codec.withAlternative(fullCodec, tagCodec.xmap(File.Inline::new, File::values));

		}

		@Override
		public @Nullable E element(ResourceLocation id, boolean required) {
			return elementGetter.element(id, required);
		}

		@Override
		public @Nullable Collection<E> tag(ResourceLocation id) {
			return tagGetter.tag(id);
		}

		public DataResult<ImmutableList<E>> resolve(TagEntry entry) {

			ImmutableList.Builder<E> builder = ImmutableList.builder();
			boolean missing = !entry.build(this, builder::add);

			ImmutableList<E> found = builder.build();
			DataResult<ImmutableList<E>> result = DataResult.success(found);

			if (missing) {
				result = DataResult.error(() -> this + " is missing the reference: \"" + entry + "\"");
			}

			return result.setPartial(found);

		}

		public DataResult<ImmutableList<E>> resolve(List<TagEntry> entries) {

			ImmutableList.Builder<E> builder = ImmutableList.builder();
			List<TagEntry> missing = new ObjectArrayList<>();

			for (var entry : entries) {

				if (!entry.build(this, builder::add)) {
					missing.add(entry);
				}

			}

			ImmutableList<E> found = builder.build();
			DataResult<ImmutableList<E>> result = DataResult.success(found);

			if (!missing.isEmpty()) {
				result = DataResult.error(() -> this + " is missing the following references: [" + missing.stream().map(TagEntry::toString).map(str -> "\"" + str + "\"").collect(Collectors.joining(", ")) + "]");
			}

			return result.setPartial(found);

		}

		public File<E> merge(File<E> first, File<E> second) {

			TagLike.Builder<E> values = new Builder<>(this);

			if (!second.replace()) {
				values.addAll(first.values().entries());
			}

			values.addAll(second.values().entries());
			return second.withValues(values.build());

		}

		public static <T> Source<T> of(Supplier<String> name, ElementGetter<T> elementGetter, TagGetter<T> tagGetter) {
			return new Source<>(elementGetter, tagGetter) {

				@Override
				public String toString() {
					return name.get();
				}

			};
		}

		public static <K, V, M extends ContentAndTagManager<K, V>> Source<V> fromManager(Supplier<M> managerSupplier, Function<ResourceLocation, K> toKey) {
			return of(
				Suppliers.memoize(managerSupplier.get()::toString),
				(id, required) -> managerSupplier.get().getAsResult(toKey.apply(id)).result().orElse(null),
				id -> managerSupplier.get().getTagAsResult(id).result().orElse(null)
			);
		}

		public static <V, M extends ContentAndTagManager<ResourceLocation, V>> Source<V> fromManager(Supplier<M> managerSupplier) {
			return fromManager(managerSupplier, Function.identity());
		}

		public static <T> Source<T> fromRegistry(HolderLookup.RegistryLookup<T> registry) {

			//noinspection unchecked
			ResourceKey<? extends Registry<T>> registryKey = (ResourceKey<? extends Registry<T>>) registry.key();

			return of(
				() -> "Registry \"" + registryKey.location() + "\"",
				(id, required) -> registry.get(ResourceKey.create(registryKey, id))
					.map(Holder.Reference::value)
					.orElse(null),
				id -> registry.get(TagKey.create(registryKey, id))
					.map(Source::unwrapHolders)
					.orElse(null)
			);

		}

		private static <T> List<T> unwrapHolders(HolderSet<T> holders) {
			return holders.stream()
				.map(Holder::value)
				.toList();
		}

	}



}
