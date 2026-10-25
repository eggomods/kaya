package io.github.eggohito.neo_apoli.client.integration;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import io.github.eggohito.neo_apoli.client.event.ConfigCategoryRegistrant;
import io.github.eggohito.neo_apoli.hud.element.custom.ResourceBarHudElement;
import io.github.eggohito.neo_apoli.power.custom.ModifyPlayerSpawnPower;
import io.github.eggohito.neo_apoli.power.custom.MultiplePower;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public final class ClientConfigIntegrations {

	public static void init() {

		ResourceBarHudElement.Config.INSTANCE.loadFromFile();

		ConfigCategoryRegistrant.HUD_ELEMENT_TYPE.register(resourceBarCategory());
		ConfigCategoryRegistrant.POWER_TYPE.register(modifyPlayerSpawnCategory());
		ConfigCategoryRegistrant.POWER_TYPE.register(multipleConfigCategory());

	}

	private static ConfigCategoryRegistrant.Entry resourceBarCategory() {
		return new ConfigCategoryRegistrant.Entry() {

			@Override
			public void addGroup(Consumer<OptionGroup> adder) {

				var offsetX = Option.<Integer>createBuilder()
					.name(Component.translatable("config.neo-apoli.type.hud_element.resource_bar.option.offset_x.name"))
					.description(OptionDescription.of(Component.translatable("config.neo-apoli.type.hud_element.resource_bar.option.offset_x.description")))
					.binding(ResourceBarHudElement.Config.INSTANCE.offsetX.asBinding())
					.controller(IntegerFieldControllerBuilder::create);
				var offsetY = Option.<Integer>createBuilder()
					.name(Component.translatable("config.neo-apoli.type.hud_element.resource_bar.option.offset_y.name"))
					.description(OptionDescription.of(Component.translatable("config.neo-apoli.type.hud_element.resource_bar.option.offset_y.description")))
					.binding(ResourceBarHudElement.Config.INSTANCE.offsetY.asBinding())
					.controller(IntegerFieldControllerBuilder::create);

				adder.accept(OptionGroup.createBuilder()
					.name(Component.translatable("config.neo-apoli.type.hud_element.resource_bar.name"))
					.description(OptionDescription.of(Component.translatable("config.neo-apoli.type.hud_element.resource_bar.description")))
					.option(offsetX.build())
					.option(offsetY.build())
					.build());

			}

			@Override
			public void save() {
				ResourceBarHudElement.Config.INSTANCE.saveToFile();
			}

		};
	}

	private static ConfigCategoryRegistrant.Entry modifyPlayerSpawnCategory() {
		return new ConfigCategoryRegistrant.Entry() {

			@Override
			public void addGroup(Consumer<OptionGroup> adder) {

				var horizontalStep = Option.<Integer>createBuilder()
					.name(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.option.horizontal_step.name"))
					.description(OptionDescription.of(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.option.horizontal_step.description")))
					.binding(ModifyPlayerSpawnPower.Config.INSTANCE.horizontalStep.asBinding())
					.controller(option -> IntegerFieldControllerBuilder.create(option)
						.min(0));
				var verticalStep = Option.<Integer>createBuilder()
					.name(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.option.vertical_step.name"))
					.description(OptionDescription.of(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.option.vertical_step.description")))
					.binding(ModifyPlayerSpawnPower.Config.INSTANCE.verticalStep.asBinding())
					.controller(option -> IntegerFieldControllerBuilder.create(option)
						.min(0));
				var radius = Option.<Integer>createBuilder()
					.name(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.option.radius.name"))
					.description(OptionDescription.of(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.option.radius.description")))
					.binding(ModifyPlayerSpawnPower.Config.INSTANCE.radius.asBinding())
					.controller(option -> IntegerFieldControllerBuilder.create(option)
						.min(0));

				adder.accept(OptionGroup.createBuilder()
					.name(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.name"))
					.description(OptionDescription.of(Component.translatable("config.neo-apoli.type.power.modify_player_spawn.description")))
					.option(horizontalStep.build())
					.option(verticalStep.build())
					.option(radius.build())
					.build());

			}

			@Override
			public void save() {
				ModifyPlayerSpawnPower.Config.INSTANCE.saveToFile();
			}

		};
	}

	private static ConfigCategoryRegistrant.Entry multipleConfigCategory() {
		return new ConfigCategoryRegistrant.Entry() {

			//  TODO: Re-add the option for being able to modify the list of ignored fields
			//        (currently removed due to how list options are implemented in YACL's API)
			@Override
			public void addGroup(Consumer<OptionGroup> adder) {

			}

			@Override
			public void save() {
				MultiplePower.Config.INSTANCE.saveToFile();
			}

		};
	}

}
