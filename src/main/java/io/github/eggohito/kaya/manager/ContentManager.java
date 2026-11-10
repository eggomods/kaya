package io.github.eggohito.kaya.manager;

import com.mojang.serialization.DataResult;

import java.util.function.Function;

public interface ContentManager<K, V> extends Manager {

	DataResult<V> getAsResult(K key, Function<K, String> onError);

	DataResult<K> getKeyAsResult(V value, Function<V, String> onError);

	default DataResult<V> getAsResult(K key) {
		return this.getAsResult(key, k -> "Missing key: \"" + key + "\"");
	}

	default DataResult<K> getKeyAsResult(V value) {
		return this.getKeyAsResult(value, v -> "Unregistered value: " + value);
	}

	default V get(K key) {
		return this.getAsResult(key).getOrThrow();
	}

	default K getKey(V value) {
		return this.getKeyAsResult(value).getOrThrow();
	}

	default boolean contains(K key) {
		return this.getAsResult(key).isSuccess();
	}

	default boolean containsKey(V value) {
		return this.getKeyAsResult(value).isSuccess();
	}

	Iterable<K> keys();

	Iterable<V> values();

	int size();

}
