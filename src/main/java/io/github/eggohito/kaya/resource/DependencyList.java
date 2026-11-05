package io.github.eggohito.kaya.resource;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class DependencyList {

	private final ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
	private final ResourceLocation id;

	private ImmutableSet<ResourceLocation> dependencies;
	private boolean frozen;

	public DependencyList(ResourceLocation id) {
		this.id = id;
	}

	public void add(ResourceLocation dependency) {

		if (frozen) {
			throw new IllegalStateException("Cannot add dependency to a frozen dependency list");
		}

		else if (id.equals(dependency)) {
			throw new IllegalArgumentException("Attempted to add recursive dependency \"" + dependency + "\"");
		}

		else {
			this.builder.add(dependency);
		}

	}

	public ImmutableSet<ResourceLocation> freezeAndGet() {

		if (frozen) {
			return Objects.requireNonNull(this.dependencies, "Dependency list was not frozen properly");
		}

		else {

			this.dependencies = this.builder.build();
			this.frozen = true;

			return this.dependencies;

		}

	}

}
