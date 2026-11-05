package io.github.eggohito.kaya.resource;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

public final class DependencyList {

	private final Set<ResourceLocation> dependencies = new ObjectLinkedOpenHashSet<>();
	private final ResourceLocation id;

	private ImmutableSet<ResourceLocation> frozen;

	public DependencyList(ResourceLocation id) {
		this.id = id;
	}

	public void add(ResourceLocation dependency) {

		if (frozen != null) {
			throw new IllegalStateException("Cannot add dependency to a frozen dependency list");
		}

		else if (id.equals(dependency)) {
			throw new IllegalArgumentException("Tried to add recursive dependency \"" + dependency + "\"");
		}

		else {
			this.dependencies.add(dependency);
		}

	}

	public ImmutableSet<ResourceLocation> freezeAndGet() {

		if (frozen != null) {
			return Objects.requireNonNull(this.frozen, "Dependency list was not frozen properly");
		}

		else {

			this.frozen = ImmutableSet.copyOf(this.dependencies);
			this.dependencies.clear();

			return this.frozen;

		}

	}

}
