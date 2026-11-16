package io.github.eggohito.kaya.util.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import io.github.eggohito.kaya.util.ResourceLocationUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.function.Function;

public final class FixedRegistryAlias<T> extends CustomRegistryAlias<T> {

	private final Registry<T> registry;

	private FixedRegistryAlias(ResourceLocationAlias parent, Registry<T> registry) {
		super(parent, registry.key().location(), registry::getKey, registry::getValue);
		this.registry = registry;
	}

	@Override
	public Codec<T> createCodec(String defaultNamespace) {
		return this.createReferenceCodec(defaultNamespace).flatComapMap(Holder.Reference::value, value -> safeCastToReference(registry.wrapAsHolder(value)));
	}

	public DataResult<Holder.Reference<T>> resolveReference(ResourceLocation id) {
		return registry.get(this.resolve(id, registry::containsKey))
			.map(DataResult::success)
			.orElse(DataResult.error(() -> "Unknown key in registry \"" + registryId + "\": \"" + id + "\""));
	}

	public Codec<Holder.Reference<T>> createReferenceCodec(String defaultNamespace) {

		Codec<Holder.Reference<T>> codec = ResourceLocationUtil.codecWithDefaultNamespace(defaultNamespace).comapFlatMap(
			this::resolveReference,
			reference -> reference.key().location()
		);

		return ExtraCodecs.overrideLifecycle(
			codec,
			holder -> registry.registrationInfo(holder.key())
				.map(RegistrationInfo::lifecycle)
				.orElseGet(Lifecycle::experimental)
		);

	}

	public Codec<Holder.Reference<T>> createReferenceCodec() {
		return this.createReferenceCodec(ResourceLocation.DEFAULT_NAMESPACE);
	}

	public Codec<Holder<T>> createHolderCodec(String defaultNamespace) {
		return createReferenceCodec(defaultNamespace).flatComapMap(Function.identity(), this::safeCastToReference);
	}

	public Codec<Holder<T>> createHolderCodec() {
		return this.createHolderCodec(ResourceLocation.DEFAULT_NAMESPACE);
	}

	public StreamCodec<RegistryFriendlyByteBuf, T> createRegistryStreamCodec() {
		return ByteBufCodecs.registry(registry.key());
	}

	private DataResult<Holder.Reference<T>> safeCastToReference(Holder<T> holder) {
		return holder instanceof Holder.Reference<T> reference
			? DataResult.success(reference)
			: DataResult.error(() -> "Unregistered holder in registry \"" + registryId + "\": " + holder);
	}

	public static <T> FixedRegistryAlias<T> of(Registry<T> registry, ResourceLocationAlias parent) {
		return new FixedRegistryAlias<>(parent, registry);
	}

	public static <T> FixedRegistryAlias<T> of(Registry<T> registry) {
		return new FixedRegistryAlias<>(NO_OP, registry);
	}

}
