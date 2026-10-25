package io.github.eggohito.neo_apoli.util.alias;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.eggohito.neo_apoli.exception.AliasAlreadyTakenException;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
public class CustomRegistryAlias<T> {

	protected final ResourceLocation registryId;
	protected final ResourceLocationAlias aliases;
	protected final Function<T, ResourceLocation> keyGetter;
	protected final Function<ResourceLocation, T> valueGetter;

	public CustomRegistryAlias(ResourceLocation registryId, BiMap<ResourceLocation, T> map) {
		this(registryId, new ResourceLocationAlias(), map.inverse()::get, map::get);
	}

	public boolean hasNamespaceAlias(String namespace) {
		return aliases.getNamespaces().hasAlias(namespace);
	}

	public boolean hasPathAlias(T t) {
		ResourceLocation key = keyGetter.apply(t);
		return key != null
			&& aliases.getPaths().hasAlias(key.getPath());
	}

	public boolean hasAlias(T t) {
		ResourceLocation key = keyGetter.apply(t);
		return key != null
			&& aliases.hasAlias(key);
	}

	public void addNamespaceAlias(String from, String to) {

		try {
			aliases.getNamespaces().addAlias(from, to);
		}

		catch (AliasAlreadyTakenException e) {
			throw new AliasAlreadyTakenException(e.getMessage() + " in registry \"" + registryId + "\"");
		}

	}

	public void addPathAlias(String from, T to) {

		try {

			ResourceLocation toKey = keyGetter.apply(to);

			if (toKey != null) {
				aliases.getPaths().addAlias(from, toKey.getPath());
			}

		}

		catch (AliasAlreadyTakenException e) {
			throw new AliasAlreadyTakenException(e.getMessage() + " in registry \"" + registryId + "\"");
		}

	}

	public void addAlias(ResourceLocation from, T to) {

		try {

			ResourceLocation toKey = keyGetter.apply(to);

			if (toKey != null) {
				aliases.addAlias(from, toKey);
			}

		}

		catch (AliasAlreadyTakenException e) {
			throw new AliasAlreadyTakenException(e.getMessage() + " in registry \"" + registryId + "\"");
		}

	}

	public DataResult<T> resolveValue(ResourceLocation id) {
		return Optional.ofNullable(valueGetter.apply(aliases.resolve(id, k -> valueGetter.apply(k) != null)))
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
