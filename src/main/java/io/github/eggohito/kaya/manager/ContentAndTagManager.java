package io.github.eggohito.kaya.manager;

import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public interface ContentAndTagManager<K, V> extends ContentManager<K, V> {

	DataResult<List<V>> getTagAsResult(ResourceLocation id, Function<ResourceLocation, String> onError);

	default DataResult<List<V>> getTagAsResult(ResourceLocation id) {
		return this.getTagAsResult(id, i -> "Unknown tag: \"" + i + "\"");
	}

	default List<V> getTag(ResourceLocation id) {
		return this.getTagAsResult(id).mapOrElse(Function.identity(), error -> List.of());
	}

	Iterable<ResourceLocation> tags();

}
