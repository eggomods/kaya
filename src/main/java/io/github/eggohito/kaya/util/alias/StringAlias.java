package io.github.eggohito.kaya.util.alias;

import io.github.eggohito.kaya.exception.AliasAlreadyTakenException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;

public class StringAlias {

	private final Map<String, String> aliases;

	public StringAlias() {
		this.aliases = new Object2ObjectOpenHashMap<>();
	}

	public String resolveAlias(String value) {
		return Objects.requireNonNullElse(this.aliases.get(value), value);
	}

	public void addAlias(String from, String to) {

		String aliased = this.aliases.putIfAbsent(from, to);

		if (aliased != null) {
			throw new AliasAlreadyTakenException(from, to, () -> aliased);
		}

	}

}
