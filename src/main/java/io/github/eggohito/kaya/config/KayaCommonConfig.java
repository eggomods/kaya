package io.github.eggohito.kaya.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.mixin.access.CommandSourceStackAccessor;
import io.github.eggohito.kaya.util.Reporter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.TriState;
import org.quiltmc.parsers.json.JsonFormat;

@SuppressWarnings("UnstableApiUsage")
public final class KayaCommonConfig extends AbstractJsonCodecConfig<KayaCommonConfig> {

	public static final KayaCommonConfig INSTANCE = new KayaCommonConfig();

	public final ConfigEntry<TriState> performHandshake = register("perform_handshake", TriState.DEFAULT, Codec.lazyInitialized(() -> KayaCodecs.TRI_STATE));
	public final ConfigEntry<Command> command = register("command", Command.DEFAULT, Command.CODEC);
	public final ConfigEntry<PlaceholderIdentifier> placeholderIdentifier = register("placeholder_identifier", PlaceholderIdentifier.DEFAULT, PlaceholderIdentifier.CODEC);
	public final ConfigEntry<ReporterConfig> reporter = register("reporter", ReporterConfig.DEFAULT, ReporterConfig.CODEC);

	private KayaCommonConfig() {
		super(FabricLoader.getInstance().getConfigDir().resolve("kaya/common.json5"), JsonFormat.JSON5);
	}

	@AllArgsConstructor
	@Accessors(fluent = true)
	@Data
	public static final class Command {

		public static final Command DEFAULT = new Command(false, 2);

		public static final Codec<Command> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("show_output").forGetter(Command::showOutput),
			Codec.intRange(0, 4).fieldOf("permission_level").forGetter(Command::permissionLevel)
		).apply(instance, Command::new));

		private boolean showOutput;
		private int permissionLevel;

		public CommandSourceStack sanitizeSource(CommandSourceStack commandSource) {
			CommandSource output = ((CommandSourceStackAccessor) commandSource).getOutput();
			return commandSource
				.withSource(showOutput() ? output : CommandSource.NULL)
				.withPermission(permissionLevel());
		}

	}

	@AllArgsConstructor
	@Accessors(fluent = true)
	@Data
	public static final class PlaceholderIdentifier {

		public static final PlaceholderIdentifier DEFAULT = new PlaceholderIdentifier(true);

		public static final Codec<PlaceholderIdentifier> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.BOOL.fieldOf("enabled").forGetter(PlaceholderIdentifier::enabled))
			.apply(instance, PlaceholderIdentifier::new)
		);

		private boolean enabled;

	}

	@AllArgsConstructor
	@Accessors(fluent = true)
	@Data
	public static final class ReporterConfig {

		public static final ReporterConfig DEFAULT = new ReporterConfig(Reporter.TreeView.EXPANDED);

		@Deprecated
		public static final Codec<ReporterConfig> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Reporter.TreeView.CODEC.fieldOf("tree_display").forGetter(ReporterConfig::treeView))
			.apply(instance, ReporterConfig::new)
		);

		public static final Codec<ReporterConfig> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Reporter.TreeView.CODEC.fieldOf("tree_view").forGetter(ReporterConfig::treeView))
			.apply(instance, ReporterConfig::new)
		);

		public static final Codec<ReporterConfig> CODEC = Codec.withAlternative(DIRECT_CODEC, LEGACY_CODEC);

		private Reporter.TreeView treeView;

	}

}
