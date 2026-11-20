package io.github.eggohito.kaya.util.services.manager;

import com.google.common.collect.ImmutableMap;
import io.github.eggohito.kaya.manager.Manager;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public abstract class AbstractManagers implements Managers {

	private final ImmutableMap<ResourceKey<? extends Manager>, Manager> map;

	protected AbstractManagers(UnaryOperator<Builder> operator) {
		this.map = operator.apply(new Builder()).builder.build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <M extends Manager> M get(ResourceKey<? extends M> key) {
		return (M) this.map.get(key);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[" + this.map.keySet().stream().map(key -> "'%s'/'%s'".formatted(key.registry(), key.location())).collect(Collectors.joining(", ")) + "]";
	}

	public static final class Builder {

		private final ImmutableMap.Builder<ResourceKey<? extends Manager>, Manager> builder = ImmutableMap.builder();

		private Builder() {

		}

		public <M extends Manager> Builder put(ResourceKey<M> key, M manager) {
			this.builder.put(key, manager);
			return this;
		}

	}

}
