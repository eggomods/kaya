package io.github.eggohito.kaya.util.tag;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.DataResult;
import io.github.eggohito.kaya.util.manager.ContentAndTagManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class TagLookup<E> implements TagEntry.Lookup<E>, ElementGetter<E>, TagGetter<E> {

	private final Supplier<String> name;

	private final ElementGetter<E> element;
	private final TagGetter<E> tag;

	private TagLookup(Supplier<String> name, ElementGetter<E> element, TagGetter<E> tag) {
		this.name = name;
		this.element = element;
		this.tag = tag;
	}

	@Override
	public @Nullable E element(ResourceLocation id, boolean required) {
		return element.element(id, required);
	}

	@Override
	public @Nullable Collection<E> tag(ResourceLocation id) {
		return tag.tag(id);
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

	public DataResult<Unit> validate(TagEntry entry) {
		return this.resolve(entry).map(ignored -> Unit.INSTANCE);
	}

	public DataResult<Unit> validate(List<TagEntry> entries) {
		return this.resolve(entries).map(ignored -> Unit.INSTANCE);
	}

	@Override
	public String toString() {
		return name.get();
	}

	public static <T> TagLookup<T> of(String name, ElementGetter<T> element, TagGetter<T> tag) {
		return new TagLookup<>(Suppliers.memoize(() -> name), element, tag);
	}

	public static <T> TagLookup<T> of(Supplier<String> name, ElementGetter<T> element, TagGetter<T> tag) {
		return new TagLookup<>(name, element, tag);
	}

	public static <V, K, M extends ContentAndTagManager<K, V>> TagLookup<V> fromManager(Supplier<M> managerSupplier, Function<ResourceLocation, K> toKey) {
		return of(
			() -> managerSupplier.get().toString(),
			(id, required) -> managerSupplier.get().getAsResult(toKey.apply(id)).result().orElse(null),
			id -> managerSupplier.get().getTagAsResult(id).result().orElse(null)
		);
	}

	public static <V, M extends ContentAndTagManager<ResourceLocation, V>> TagLookup<V> fromManager(Supplier<M> managerSupplier) {
		return fromManager(managerSupplier, Function.identity());
	}

	public static <T> TagLookup<T> fromRegistry(HolderLookup.RegistryLookup<T> registry) {

		//noinspection unchecked
		ResourceKey<? extends Registry<T>> registryKey = (ResourceKey<? extends Registry<T>>) registry.key();

		return of(
			"Registry \"" + registryKey.location() + "\"",
			(id, required) -> registry.get(ResourceKey.create(registryKey, id))
				.map(Holder.Reference::value)
				.orElse(null),
			id -> registry.get(TagKey.create(registryKey, id))
				.map(TagLookup::unwrap)
				.orElse(null)
		);

	}

	private static <T> List<T> unwrap(HolderSet<T> holders) {
		return holders.stream()
			.map(Holder::value)
			.toList();
	}

}
