package io.github.eggohito.kaya.util.alias;

import io.github.eggohito.kaya.exception.AliasAlreadyTakenException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ResourceLocationAlias {

	public static final ResourceLocationAlias NO_OP = new ResourceLocationAlias() {

		@Override
		public ResourceLocation resolve(ResourceLocation id, Predicate<ResourceLocation> until) {
			return id;
		}

		@Override
		public void addAlias(ResourceLocation from, ResourceLocation to) {

		}

	};

	private final ResourceLocationAlias parent;
	private final Map<ResourceLocation, ResourceLocation> ids;
	private final StringAlias namespaces;
	private final StringAlias paths;

	protected ResourceLocationAlias(ResourceLocationAlias parent, Map<ResourceLocation, ResourceLocation> ids, StringAlias namespaces, StringAlias paths) {
		this.parent = parent;
		this.ids = ids;
		this.namespaces = namespaces;
		this.paths = paths;
	}

	public ResourceLocationAlias(ResourceLocationAlias parent) {
		this(parent, new Object2ObjectOpenHashMap<>(), new StringAlias(), new StringAlias());
	}

	public ResourceLocationAlias() {
		this(NO_OP);
	}

	public ResourceLocation resolve(ResourceLocation id, Predicate<ResourceLocation> until) {

		ResourceLocation aliasedId = this.parent.resolve(id, until);

		if (!aliasedId.equals(id)) {
			return aliasedId;
		}

		for (Resolver resolver : Resolver.values()) {

			aliasedId = resolver.apply(this, id);

			if (until.test(aliasedId)) {
				return aliasedId;
			}

		}

		return id;

	}

	public void addAlias(ResourceLocation from, ResourceLocation to) {

		ResourceLocation aliased = this.ids.putIfAbsent(from, to);

		if (aliased != null) {
			throw new AliasAlreadyTakenException(from.toString(), to.toString(), aliased::toString);
		}

	}

	public void addNamespaceAlias(String from, String to) {
		this.namespaces.addAlias(from, to);
	}

	public void addPathAlias(String from, String to) {
		this.paths.addAlias(from, to);
	}

	private enum Resolver implements BiFunction<ResourceLocationAlias, ResourceLocation, ResourceLocation> {

		NO_OP {

			@Override
			public ResourceLocation apply(ResourceLocationAlias aliases, ResourceLocation id) {
				return id;
			}

		},

		IDENTIFIER {

			@Override
			public ResourceLocation apply(ResourceLocationAlias aliases, ResourceLocation id) {
				return Objects.requireNonNullElse(aliases.ids.get(id), id);
			}

		},

		NAMESPACE {

			@Override
			public ResourceLocation apply(ResourceLocationAlias aliases, ResourceLocation id) {
				return ResourceLocation.fromNamespaceAndPath(aliases.namespaces.resolveAlias(id.getNamespace()), id.getPath());
			}

		},

		PATH {

			@Override
			public ResourceLocation apply(ResourceLocationAlias aliases, ResourceLocation id) {
				return id.withPath(aliases.paths.resolveAlias(id.getPath()));
			}

		},

		NAMESPACE_AND_PATH {

			@Override
			public ResourceLocation apply(ResourceLocationAlias aliases, ResourceLocation id) {

				String aliasedNamespace = aliases.namespaces.resolveAlias(id.getNamespace());
				String aliasedPath = aliases.paths.resolveAlias(id.getPath());

				return ResourceLocation.fromNamespaceAndPath(aliasedNamespace, aliasedPath);

			}

		}

	}

}
