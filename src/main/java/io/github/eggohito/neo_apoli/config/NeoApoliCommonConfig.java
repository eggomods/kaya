package io.github.eggohito.neo_apoli.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.mixin.access.CommandSourceStackAccessor;
import io.github.eggohito.neo_apoli.util.Reporter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.TriState;
import org.quiltmc.parsers.json.JsonFormat;

@SuppressWarnings("UnstableApiUsage")
public final class NeoApoliCommonConfig extends AbstractJsonCodecConfig<NeoApoliCommonConfig> {

	public static final NeoApoliCommonConfig INSTANCE = new NeoApoliCommonConfig();

	public final ConfigEntry<TriState> performHandshake = register("perform_handshake", TriState.DEFAULT, Codec.lazyInitialized(() -> NeoApoliCodecs.TRI_STATE));
	public final ConfigEntry<Command> command = register("command", Command.DEFAULT, Command.CODEC);
	public final ConfigEntry<PlaceholderIdentifier> placeholderIdentifier = register("placeholder_identifier", PlaceholderIdentifier.DEFAULT, PlaceholderIdentifier.CODEC);
	public final ConfigEntry<ReporterConfig> reporter = register("reporter", ReporterConfig.DEFAULT, ReporterConfig.CODEC);

	private NeoApoliCommonConfig() {
		super(FabricLoader.getInstance().getConfigDir().resolve("neo-apoli/common.json5"), JsonFormat.JSON5);
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

		public static final ReporterConfig DEFAULT = new ReporterConfig(Reporter.TreeDisplay.EXPANDED);

		public static final Codec<ReporterConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Reporter.TreeDisplay.CODEC.fieldOf("tree_display").forGetter(ReporterConfig::treeDisplay))
			.apply(instance, ReporterConfig::new)
		);

		private Reporter.TreeDisplay treeDisplay;

	}

}
