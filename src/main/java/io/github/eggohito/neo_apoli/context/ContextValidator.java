package io.github.eggohito.neo_apoli.context;

import com.google.common.collect.Sets;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.visitor.Visitor;
import io.github.eggohito.neo_apoli.util.Reporter;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ContextValidator {

	@Getter
	private final Parameters parameters;
	@Getter
	private final Reporter reporter;

	private final Optional<HolderLookup.Provider> resolver;
	private final Set<ResourceKey<?>> visited;

	ContextValidator(Parameters parameters, Reporter reporter, Optional<HolderLookup.Provider> resolver, Set<ResourceKey<?>> visited) {
		this.parameters = parameters;
		this.reporter = reporter;
		this.resolver = resolver;
		this.visited = visited;
	}

	public ContextValidator(Parameters parameters, Reporter reporter, @NotNull HolderLookup.Provider resolver) {
		this(parameters, reporter, Optional.of(resolver), new ObjectOpenHashSet<>());
	}

	public ContextValidator(Parameters parameters, Reporter reporter) {
		this(parameters, reporter, Optional.empty(), new ObjectOpenHashSet<>());
	}

	public ContextValidator withParams(Parameters... params) {
		return new ContextValidator(this.parameters().mergeAll(params), this.reporter(), this.resolver, this.visited);
	}

	public ContextValidator withResolver(@NotNull HolderLookup.Provider resolver) {
		return new ContextValidator(this.parameters(), this.reporter(), Optional.of(resolver), this.visited);
	}

	public ContextValidator forChild(String path) {
		return new ContextValidator(this.parameters(), this.reporter().forChild(path), this.resolver, new ObjectOpenHashSet<>());
	}

	public ContextValidator visitChild(String path, ResourceKey<?> key) {
		this.visited.add(key);
		return new ContextValidator(this.parameters(), this.reporter().forChild(path), this.resolver, this.visited);
	}

	public HolderLookup.Provider resolver() {
		return resolver.orElseThrow(() -> new UnsupportedOperationException("References are not allowed!"));
	}

	public Visitor<ResourceKey<?>> visitor() {
		return new Visitor<>() {

			@Override
			public boolean contains(ResourceKey<?> element) {
				return visited.contains(element);
			}

			@Override
			public boolean push(ResourceKey<?> element) {
				return visited.add(element);
			}

			@Override
			public void pop(ResourceKey<?> element) {
				visited.remove(element);
			}

		};
	}

	public boolean allowsReferences() {
		return resolver.isPresent();
	}

	public boolean hasVisited(ResourceKey<?> key) {
		return visited.contains(key);
	}

	public void reportProblem(String message) {
		this.reporter.report(message);
	}

	public void validate(ContextUser user) {

		if (parameters().isEmpty()) {
			return;
		}

		Set<ContextParameter<?>> missing = Sets.difference(user.getRequiredParameters(), parameters().allowed());

		if (!missing.isEmpty()) {
			this.reportProblem("The following parameters are not provided: [" + missing.stream().map(ContextParameter::asDisplayString).collect(Collectors.joining(", ")) + "]");
		}

	}

	@Accessors(fluent = true)
	@Getter
	public static final class Parameters {

		public static final Parameters EMPTY = new Parameters(Set.of(), Set.of());

		private final Set<ContextParameter<?>> required;
		private final Set<ContextParameter<?>> allowed;

		private Parameters(Set<ContextParameter<?>> required, Set<ContextParameter<?>> optional) {
			this.required = Set.copyOf(required);
			this.allowed = Sets.union(required, optional);
		}

		public Parameters merge(Parameters that) {

			Set<ContextParameter<?>> required = Sets.union(this.required(), that.required());
			Set<ContextParameter<?>> optional = Sets.difference(Sets.union(this.allowed(), that.allowed()), required);

			Parameters.Builder builder = new Parameters.Builder();

			required.forEach(builder::required);
			optional.forEach(builder::optional);

			return builder.build();

		}

		public Parameters mergeAll(Parameters... others) {
			return Arrays.stream(others).reduce(this, Parameters::merge);
		}

		public boolean isEmpty() {
			return this == EMPTY;
		}

		public static final class Builder {

			private final Set<ContextParameter<?>> required = new ObjectOpenHashSet<>();
			private final Set<ContextParameter<?>> optional = new ObjectOpenHashSet<>();

			public Parameters.Builder required(ContextParameter<?> parameter) {

				if (!optional.contains(parameter)) {
					this.required.add(parameter);
				}

				return this;

			}

			public Parameters.Builder optional(ContextParameter<?> parameter) {

				if (!required.contains(parameter)) {
					this.optional.add(parameter);
				}

				return this;

			}

			public Parameters build() {
				return required.isEmpty() && optional.isEmpty()
					? EMPTY
					: new Parameters(required, optional);
			}

		}

	}

}
