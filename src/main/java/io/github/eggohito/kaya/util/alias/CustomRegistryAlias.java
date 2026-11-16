package io.github.eggohito.kaya.util.alias;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.eggohito.kaya.exception.AliasAlreadyTakenException;
import io.github.eggohito.kaya.util.ResourceLocationUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class CustomRegistryAlias<T> extends ResourceLocationAlias {

	protected final ResourceLocation registryId;
	protected final Function<T, ResourceLocation> keyGetter;
	protected final Function<ResourceLocation, T> valueGetter;

	public CustomRegistryAlias(ResourceLocationAlias parent, ResourceLocation registryId, Function<T, ResourceLocation> keyGetter, Function<ResourceLocation, T> valueGetter) {
		super(parent);
		this.registryId = registryId;
		this.keyGetter = keyGetter;
		this.valueGetter = valueGetter;
	}

	public CustomRegistryAlias(ResourceLocation registryId, Function<T, ResourceLocation> keyGetter, Function<ResourceLocation, T> valueGetter) {
		this(ResourceLocationAlias.NO_OP, registryId, keyGetter, valueGetter);
	}

	public CustomRegistryAlias(ResourceLocation registryId, BiMap<ResourceLocation, T> map) {
		this(registryId, map.inverse()::get, map::get);
	}

	public void addPathAlias(String from, T to) {

		try {
			this.addPathAlias(from, Objects.requireNonNull(this.keyGetter.apply(to), "Unknown element in registry \"" + registryId + "\": " + to).getPath());
		}

		catch (AliasAlreadyTakenException e) {
			throw new AliasAlreadyTakenException(e.getMessage() + " in registry \"" + registryId + "\"");
		}

	}

	public void addAlias(ResourceLocation from, T to) {

		try {
			this.addAlias(from, Objects.requireNonNull(this.keyGetter.apply(to), "Unknown element in registry \"" + registryId + "\": " + to));
		}

		catch (AliasAlreadyTakenException e) {
			throw new AliasAlreadyTakenException(e.getMessage() + " in registry \"" + registryId + "\"");
		}

	}

	public DataResult<T> resolveValue(ResourceLocation id) {
		return Optional.ofNullable(valueGetter.apply(this.resolve(id, k -> valueGetter.apply(k) != null)))
			.map(DataResult::success)
			.orElse(DataResult.error(() -> "Unknown key in registry \"" + registryId + "\": \"" + id + "\""));
	}

	public DataResult<ResourceLocation> resolveKey(T t) {
		return Optional.ofNullable(keyGetter.apply(t))
			.map(DataResult::success)
			.orElse(DataResult.error(() -> "Unknown element in registry \"" + registryId + "\": " + t));
	}

	public Codec<T> createCodec(String defaultNamespace) {
		return ResourceLocationUtil.codecWithDefaultNamespace(defaultNamespace).flatXmap(this::resolveValue, this::resolveKey);
	}

	public Codec<T> createCodec() {
		return ResourceLocation.CODEC.flatXmap(this::resolveValue, this::resolveKey);
	}

	public <B extends ByteBuf> StreamCodec<B, T> createStreamCodec() {
		return ResourceLocation.STREAM_CODEC.map(id -> resolveValue(id).getOrThrow(), t -> resolveKey(t).getOrThrow()).cast();
	}

}
