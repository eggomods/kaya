package io.github.eggohito.neo_apoli.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import io.github.eggohito.neo_apoli.mixin.access.RegistryOpsAccessor;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class CodecUtil {

	public static <E> Codec<E> mapped(Supplier<BiMap<String, E>> supplier) {
		return Codec.STRING.flatXmap(
			str -> {

				BiMap<String, E> mappedValues = supplier.get();
				E value = mappedValues.get(str);

				if (value != null) {
					return DataResult.success(value);
				}

				else {
					return DataResult.error(() -> "Expected value to be any of " + String.join(", ", mappedValues.keySet()));
				}

			},
			e -> {

				BiMap<String, E> mappedValues = supplier.get();
				String key = mappedValues.inverse().get(e);

				if (key != null) {
					return DataResult.success(key);
				}

				else {
					return DataResult.error(() -> "Value " + e + " is not associated to any keys!");
				}

			}
		);
	}

	public static <E> Codec<E> mapped(BiMap<String, E> map) {
		return mapped(Suppliers.memoize(() -> map));
	}

	public static <E extends Enum<E>> Codec<E> enumType(Class<E> enumClass) {
		return enumType(enumClass, ImmutableMap.of());
	}

	public static <E extends Enum<E>> Codec<E> enumType(Class<E> enumClass, ImmutableMap<String, E> aliases) {

		E[] enumConstants = enumClass.getEnumConstants();

		ToIntFunction<E> toOrdinal = Enum::ordinal;
		IntFunction<E> fromOrdinal = ByIdMap.continuous(toOrdinal, enumConstants, ByIdMap.OutOfBoundsStrategy.CLAMP);

		Function<E, String> toString = e -> e instanceof StringRepresentable representable
			? representable.getSerializedName()
			: e.name();

		Function<String, DataResult<E>> fromString = name -> {

			Set<String> expectedValues = new ObjectOpenHashSet<>();

			for (E enumConstant : enumConstants) {

				String constantName = enumConstant.name();
				expectedValues.add(constantName);

				if (constantName.equalsIgnoreCase(name)) {
					return DataResult.success(enumConstant);
				}

				else if (enumConstant instanceof StringRepresentable representable) {

					String representedName = representable.getSerializedName();
					expectedValues.add(representedName);

					if (representedName.equalsIgnoreCase(name)) {
						return DataResult.success(enumConstant);
					}

				}

			}

			for (var entry : aliases.entrySet()) {

				String alias = entry.getKey().toLowerCase(Locale.ROOT);
				expectedValues.add(alias);

				if (alias.equalsIgnoreCase(name)) {
					return DataResult.success(entry.getValue());
				}

			}

			return DataResult.error(() -> "Expected value to be any of " + String.join(", ", expectedValues) + " (case-insensitive)");

		};

		return ExtraCodecs.orCompressed(
			Codec.STRING.comapFlatMap(fromString, toString),
			ExtraCodecs.idResolverCodec(toOrdinal, fromOrdinal, -1)
		);

	}

	public static <T> Codec<TagKey<T>> tagWithDefaultNamespace(ResourceKey<? extends Registry<T>> registryRef, String defaultNamespace) {
		return ResourceLocationUtil.codecWithDefaultNamespace(defaultNamespace).xmap(location -> TagKey.create(registryRef, location), TagKey::location);
	}

	public static <T> Codec<TagKey<T>> hashedTagWithDefaultNamespace(ResourceKey<? extends Registry<T>> registryRef, String defaultNamespace) {
		Codec<ResourceLocation> locationCodec = ResourceLocationUtil.codecWithDefaultNamespace(defaultNamespace);
		return Codec.STRING.comapFlatMap(
			input -> input.startsWith("#")
				? locationCodec.parse(JavaOps.INSTANCE, input).map(location -> TagKey.create(registryRef, location))
				: DataResult.error(() -> "Not a tag id"),
			tag -> "#" + tag.location()
		);
	}

	public static <T> Codec<ResourceKey<T>> resourceKeyWithDefaultNamespace(ResourceKey<? extends Registry<T>> registryRef, String defaultNamespace) {
		return ResourceLocationUtil.codecWithDefaultNamespace(defaultNamespace).xmap(location -> ResourceKey.create(registryRef, location), ResourceKey::location);
	}

	public static <T, C extends Collection<T>> Codec<C> nonEmptyCollection(Codec<C> codec, Supplier<String> errorSupplier) {
		return codec.validate(collection -> collection.isEmpty() ? DataResult.error(errorSupplier) : DataResult.success(collection));
	}

	public static <T, C extends Collection<T>> Codec<C> nonEmptyCollection(Codec<C> codec) {
		return nonEmptyCollection(codec, () -> "Collection must have contents");
	}

	public static <T, S extends Set<T>> Codec<S> nonEmptySet(Codec<S> codec) {
		return nonEmptyCollection(codec, () -> "Set must have contents");
	}

	public static <T, O> Optional<HolderLookup.RegistryLookup<T>> getRegistry(RegistryOps<O> ops, ResourceKey<? extends Registry<T>> registryKey) {
		return ops.owner(registryKey)
			.filter(owner -> owner instanceof HolderLookup.RegistryLookup<T>)
			.map(owner -> (HolderLookup.RegistryLookup<T>) owner);
	}

	public static <A> Optional<HolderLookup.Provider> getRegistries(RegistryOps<A> ops) {

		if (((RegistryOpsAccessor) ops).getLookupProvider() instanceof RegistryOps.HolderLookupAdapter adapter) {
			return Optional.of(((RegistryOpsAccessor.HolderLookupAdapterAccessor) (Object) adapter).getLookupProvider());
		}

		else {
			return Optional.empty();
		}

	}

}
