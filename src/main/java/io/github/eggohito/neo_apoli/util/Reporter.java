package io.github.eggohito.neo_apoli.util;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.config.NeoApoliCommonConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.Util;
import net.minecraft.util.ProblemReporter;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class Reporter implements ProblemReporter {

	public static Reporter NO_OP = new Reporter() {

		@Override
		public @NotNull Reporter forChild(String name) {
			return this;
		}

		@Override
		public void report(String message) {

		}

	};

	@Nullable
	private final Reporter parent;
	private final List<Reporter> children;

	private final Set<String> problems;
	private final int depth;

	private final String fullPath;
	private final String path;

	protected Reporter(Reporter parent, String path) {
		this.parent = parent;
		this.children = new ObjectArrayList<>();
		this.problems = new ObjectLinkedOpenHashSet<>();
		this.depth = parent.depth + 1;
		this.fullPath = parent.fullPath + path;
		this.path = path;
	}

	public Reporter(String path) {
		this.parent = null;
		this.children = new ObjectArrayList<>();
		this.problems = new ObjectLinkedOpenHashSet<>();
		this.fullPath = path;
		this.path = path;
		this.depth = 0;
	}

	public Reporter() {
		this("");
	}

	@Override
	public @NotNull Reporter forChild(String name) {

		var child = new Reporter(this, name);
		this.children.add(child);

		return child;

	}

	@Override
	public void report(String message) {
		this.problems.add(message);
	}

	public Reporter getRoot() {

		Reporter root = this;

		while (root.parent != null) {
			root = root.parent;
		}

		return root;

	}

	public List<String> getTreeReport() {
		return this.getRoot().getTreeReportInternal();
	}

	public String getReport() {
		return String.join("\n", this.getTreeReport());
	}

	public boolean hasProblems() {

		if (!this.problems.isEmpty()) {
			return true;
		}

		for (var child : children) {

			if (child.hasProblems()) {
				return true;
			}

		}

		return false;

	}

	protected String indent(String line) {
		return "  ".repeat(this.depth) + line;
	}

	protected String getExpandedPath() {
		return this.depth > 0 ? "|-" + this.path : this.path;
	}

	protected List<String> getTreeReportInternal() {
		return NeoApoliCommonConfig.INSTANCE.reporter.get().treeDisplay().formatReport(this);
	}

	protected List<Reporter> getFlattenedChildren() {

		List<Reporter> result = new ObjectArrayList<>();

		for (var child : this.children) {
			result.add(child);
			result.addAll(child.getFlattenedChildren());
		}

		return result;

	}

	public enum TreeDisplay {

		COMPACT {

			@Override
			public List<String> formatReport(Reporter reporter) {

				List<Reporter> nodes = Util.make(reporter.getFlattenedChildren(), list -> list.addFirst(reporter));
				StringBuilder line = new StringBuilder();

				for (var node : nodes) {

					String fullPath = node.fullPath;
					Set<String> problems = node.problems;

					if (problems.isEmpty()) {
						continue;
					}

					if (!line.isEmpty()) {
						line.append("\n");
					}

					if (nodes.size() > 1) {
						line.append("  - ");
					}

					line.append(fullPath).append(": ");

					if (problems.size() == 1) {
						line.append(problems.iterator().next());
					}

					else {

						for (var problem : problems) {
							line.append("\n").append("      * ").append(problem);
						}

					}

				}

				return !line.isEmpty()
					? List.of(line.toString())
					: List.of();

			}

		},

		EXPANDED {

			@Override
			public List<String> formatReport(Reporter reporter) {

				List<String> lines = new ObjectArrayList<>();

				String path = reporter.getExpandedPath();
				boolean prepend = false;

				if (reporter.problems.size() == 1) {

					String problem = reporter.problems.iterator().next();

					if (reporter.path.isEmpty()) {
						lines.add(problem);
					}

					else {
						lines.add(reporter.indent(path + ": " + problem));
					}

				}

				else {

					for (var problem : reporter.problems) {
						prepend |= lines.add(reporter.indent("    * " + problem));
					}

				}

				for (var child : reporter.children) {
					prepend |= lines.addAll(child.getTreeReportInternal());
				}

				if (prepend && !reporter.path.isEmpty()) {
					lines.addFirst(reporter.indent(path + ": "));
				}

				return lines;

			}

		};

		public static final Codec<TreeDisplay> CODEC = CodecUtil.enumType(TreeDisplay.class);

		public abstract List<String> formatReport(Reporter reporter);

	}

	public static class Scoped extends Reporter implements AutoCloseable {

		public static final Scoped NO_OP = new Scoped(Consumers.nop()) {

			@Override
			public @NotNull Reporter forChild(String name) {
				return this;
			}

			@Override
			public void report(String message) {

			}

		};

		private final Consumer<String> handler;

		public Scoped(String path, Consumer<String> handler) {
			super(path);
			this.handler = handler;
		}

		public Scoped(Consumer<String> handler) {
			this.handler = handler;
		}

		@Override
		public void close() {

			if (this.hasProblems()) {
				handler.accept(this.getReport());
			}

		}

	}

}
