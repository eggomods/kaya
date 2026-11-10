package io.github.eggohito.kaya.manager;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;

import java.util.function.Function;

public abstract class AbstractContentManager<K, V> implements ContentManager<K, V> {

	protected volatile ImmutableMap<K, V> contents = ImmutableMap.of();

	@Override
	public DataResult<V> getAsResult(K key, Function<K, String> onError) {
		var candidate = contents.get(key);
		return candidate != null
			? DataResult.success(candidate)
			: DataResult.error(() -> onError.apply(key));
	}

	@Override
	public DataResult<K> getKeyAsResult(V value, Function<V, String> onError) {

		for (var entry : contents.entrySet()) {

			var candidateKey = entry.getKey();
			var candidateValue = entry.getValue();

			if (candidateValue == value) {
				return DataResult.success(candidateKey);
			}

		}

		return DataResult.error(() -> onError.apply(value));

	}

	@Override
	public Iterable<K> keys() {
		return contents.keySet();
	}

	@Override
	public Iterable<V> values() {
		return contents.values();
	}

	@Override
	public int size() {
		return contents.size();
	}

}
