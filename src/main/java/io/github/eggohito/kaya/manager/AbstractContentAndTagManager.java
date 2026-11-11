package io.github.eggohito.kaya.manager;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractContentAndTagManager<K, V> extends AbstractContentManager<K, V> implements ContentAndTagManager<K, V> {

	protected volatile ImmutableMap<ResourceLocation, List<V>> tags = ImmutableMap.of();

	@Override
	public DataResult<List<V>> getTagAsResult(ResourceLocation id, Function<ResourceLocation, String> onError) {
		var candidate = tags.get(id);
		return candidate != null
			? DataResult.success(candidate)
			: DataResult.error(() -> onError.apply(id));
	}

	@Override
	public DataResult<List<V>> getTagAsResult(ResourceLocation id) {
		return this.getTagAsResult(id, i -> "Unknown tag: \"" + i + "\"");
	}

	@Override
	public List<V> getTag(ResourceLocation id) {
		return this.getTagAsResult(id).mapOrElse(Function.identity(), error -> List.of());
	}

	@Override
	public Iterable<ResourceLocation> tags() {
		return tags.keySet();
	}

}
