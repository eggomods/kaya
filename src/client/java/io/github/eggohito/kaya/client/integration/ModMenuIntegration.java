package io.github.eggohito.kaya.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import io.github.eggohito.kaya.client.event.ConfigCategoryRegistrant;
import io.github.eggohito.kaya.config.KayaCommonConfig;
import io.github.eggohito.kaya.util.Reporter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@SuppressWarnings("UnstableApiUsage")
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {

			var yacl = YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.kaya"))
				.category(this.createCommonCategory())
				.category(this.createHudElementTypesCategory())
				.category(this.createPowerTypesCategory())
				.save(this::saveAll)
				.build();

			return yacl.generateScreen(screen);

		};
	}

	private void saveAll() {
		KayaCommonConfig.INSTANCE.saveToFile();
		ConfigCategoryRegistrant.POWER_TYPE.invoker().save();
		ConfigCategoryRegistrant.HUD_ELEMENT_TYPE.invoker().save();
	}

	private ConfigCategory createCommonCategory() {

		var commandsGroup = this.createCommandGroup(KayaCommonConfig.INSTANCE.command.asBinding());
		var placeholderIdentifierGroup = this.createPlaceholderIdentifierGroup(KayaCommonConfig.INSTANCE.placeholderIdentifier.asBinding());
		var reporterGroup = this.createReporterGroup(KayaCommonConfig.INSTANCE.reporter.asBinding());

		return ConfigCategory.createBuilder()
			.name(Component.translatable("config.kaya.common.name"))
			.group(commandsGroup)
			.group(placeholderIdentifierGroup)
			.group(reporterGroup)
			.build();

	}

	private ConfigCategory createHudElementTypesCategory() {

		var category = ConfigCategory.createBuilder().name(Component.translatable("config.kaya.type.hud_element.name"));
		ConfigCategoryRegistrant.HUD_ELEMENT_TYPE.invoker().addGroup(category::group);

		return category.build();

	}

	private ConfigCategory createPowerTypesCategory() {

		var category = ConfigCategory.createBuilder().name(Component.translatable("config.kaya.type.power.name"));
		ConfigCategoryRegistrant.POWER_TYPE.invoker().addGroup(category::group);

		return category.build();

	}

	private OptionGroup createCommandGroup(Binding<KayaCommonConfig.Command> binding) {

		var showOutput = Option.<Boolean>createBuilder()
			.name(Component.translatable("config.kaya.common.command.option.show_output.name"))
			.description(OptionDescription.of(Component.translatable("config.kaya.common.command.option.show_output.description")))
			.binding(binding.xmap(KayaCommonConfig.Command::showOutput, binding.getValue()::showOutput))
			.controller(option -> BooleanControllerBuilder.create(option)
				.onOffFormatter()
				.coloured(true));

		var permissionLevel = Option.<Integer>createBuilder()
			.name(Component.translatable("config.kaya.common.command.option.permission_level.name"))
			.description(OptionDescription.of(Component.translatable("config.kaya.common.command.option.permission_level.description")))
			.binding(binding.xmap(KayaCommonConfig.Command::permissionLevel, binding.getValue()::permissionLevel))
			.controller(option -> IntegerSliderControllerBuilder.create(option)
				.formatValue(ModMenuIntegration::formatPermissionLevel)
				.range(0, 4)
				.step(1));

		return OptionGroup.createBuilder()
			.name(Component.translatable("config.kaya.common.command.name"))
			.description(OptionDescription.of(Component.translatable("config.kaya.common.command.description")))
			.option(showOutput.build())
			.option(permissionLevel.build())
			.build();

	}

	private OptionGroup createPlaceholderIdentifierGroup(Binding<KayaCommonConfig.PlaceholderIdentifier> binding) {

		var enabled = Option.<Boolean>createBuilder()
			.name(Component.translatable("config.kaya.common.placeholder_identifier.option.enabled.name"))
			.description(OptionDescription.of(Component.translatable("config.kaya.common.placeholder_identifier.option.enabled.description")))
			.binding(binding.xmap(KayaCommonConfig.PlaceholderIdentifier::enabled, binding.getValue()::enabled))
			.controller(option -> BooleanControllerBuilder.create(option)
				.onOffFormatter()
				.coloured(true));

		return OptionGroup.createBuilder()
			.name(Component.translatable("config.kaya.common.placeholder_identifier.name"))
			.description(OptionDescription.of(Component.translatable("config.kaya.common.placeholder_identifier.description")))
			.option(enabled.build())
			.build();

	}

	private OptionGroup createReporterGroup(Binding<KayaCommonConfig.ReporterConfig> binding) {

		var treeView = Option.<Reporter.TreeView>createBuilder()
			.name(Component.translatable("config.kaya.common.reporter.option.tree_view.name"))
			.description(OptionDescription.of(Component.translatable("config.kaya.common.reporter.option.tree_view.description")))
			.binding(binding.xmap(KayaCommonConfig.ReporterConfig::treeView, binding.getValue()::treeView))
			.controller(option -> EnumControllerBuilder.create(option)
				.enumClass(Reporter.TreeView.class));

		return OptionGroup.createBuilder()
			.name(Component.translatable("config.kaya.common.reporter.name"))
			.description(OptionDescription.of(Component.translatable("config.kaya.common.reporter.description")))
			.option(treeView.build())
			.build();

	}

	private static Component formatPermissionLevel(int value) {

		String valueString = Integer.toString(value);
		ChatFormatting formatting = switch (value) {
			case 1 ->
				ChatFormatting.GREEN;
			case 2 ->
				ChatFormatting.YELLOW;
			case 3 ->
				ChatFormatting.GOLD;
			case 4 ->
				ChatFormatting.RED;
			default ->
				ChatFormatting.WHITE;
		};

		return Component.literal(valueString).withStyle(formatting);

	}

}
