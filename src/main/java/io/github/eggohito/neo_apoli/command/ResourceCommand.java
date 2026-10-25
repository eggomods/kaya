package io.github.eggohito.neo_apoli.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import io.github.eggohito.neo_apoli.command.argument.PowerArgument;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.PowerHolder;
import io.github.eggohito.neo_apoli.power.custom.FloatResourcePower;
import io.github.eggohito.neo_apoli.power.custom.IntResourcePower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Locale;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ResourceCommand {

	private static final Dynamic2CommandExceptionType UNGRANTED_FLOAT_POWER = new Dynamic2CommandExceptionType(
		(entityName, powerId) -> Component.translatableEscape("commands.neo-apoli.resource.get.float.ungranted_power", entityName, powerId)
	);

	private static final DynamicCommandExceptionType INVALID_FLOAT_POWER = new DynamicCommandExceptionType(
		powerId -> Component.translatableEscape("commands.neo-apoli.resource.get.float.invalid_power", powerId)
	);

	private static final Dynamic2CommandExceptionType UNGRANTED_INT_POWER = new Dynamic2CommandExceptionType(
		(entityName, powerId) -> Component.translatableEscape("commands.neo-apoli.resource.get.int.ungranted_power", entityName, powerId)
	);

	private static final DynamicCommandExceptionType INVALID_INT_POWER = new DynamicCommandExceptionType(
		powerId -> Component.translatableEscape("commands.neo-apoli.resource.get.int.invalid_power", powerId)
	);

	public static void register(CommandNode<CommandSourceStack> rootNode) {

		var baseNode = literal("resource")
			.requires(source -> source.hasPermission(2))
			.build();

		baseNode.addChild(GetArgument.node());
		baseNode.addChild(SetArgument.node());

		rootNode.addChild(baseNode);

	}

	private static final class GetArgument {

		private static CommandNode<CommandSourceStack> node() {

			var builder = literal("get")
				.then(argument("target", EntityArgument.entity())
					.then(literal("float")
						.then(argument("power", PowerArgument.power())
							.executes(GetArgument::executeFloatWithoutScale)
							.then(argument("scale", DoubleArgumentType.doubleArg())
								.executes(GetArgument::executeFloatWithScale))))
					.then(literal("int")
						.then(argument("power", PowerArgument.power())
							.executes(GetArgument::executeInt))));

			return builder.build();

		}

		private static int executeFloatWithoutScale(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
			return executeFloat(commandContext, 1.0D);
		}

		private static int executeFloatWithScale(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
			return executeFloat(commandContext, DoubleArgumentType.getDouble(commandContext, "scale"));
		}

		private static int executeFloat(CommandContext<CommandSourceStack> commandContext, double scale) throws CommandSyntaxException {

			Entity target = EntityArgument.getEntity(commandContext, "target");
			PowerHolder<?> powerHolder = PowerArgument.getPower(commandContext, "power");

			CommandSourceStack commandSource = commandContext.getSource();
			Power.Instance<?> instance = Powers.getOptional(target)
				.flatMap(powers -> powers.getOptionalInstance(powerHolder.id()))
				.orElse(null);

			if (instance == null) {
				throw UNGRANTED_FLOAT_POWER.create(target.getName(), powerHolder.id());
			}

			else if (instance instanceof FloatResourcePower.Instance floatResource) {

				int flooredValue = (int) (floatResource.getValue() * scale);
				commandSource.sendSuccess(() -> Component.translatableEscape("commands.neo-apoli.resource.get.float.success", powerHolder.id(), flooredValue, String.format(Locale.ROOT, "%.2f", scale)), false);

				return flooredValue;

			}

			else {
				throw INVALID_FLOAT_POWER.create(powerHolder.id());
			}

		}

		private static int executeInt(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

			Entity target = EntityArgument.getEntity(commandContext, "target");
			PowerHolder<?> powerHolder = PowerArgument.getPower(commandContext, "power");

			CommandSourceStack commandSource = commandContext.getSource();
			Power.Instance<?> instance = Powers.getOptional(target)
				.flatMap(powers -> powers.getOptionalInstance(powerHolder.id()))
				.orElse(null);

			if (instance == null) {
				throw UNGRANTED_INT_POWER.create(target.getName(), powerHolder.id());
			}

			else if (instance instanceof IntResourcePower.Instance intResource) {

				int value = intResource.getValue();
				commandSource.sendSuccess(() -> Component.translatableEscape("commands.neo-apoli.resource.get.int.success", powerHolder.id(), value), false);

				return value;

			}

			else {
				throw INVALID_INT_POWER.create(powerHolder.id());
			}

		}

	}

	private static final class SetArgument {

		private static CommandNode<CommandSourceStack> node() {

			var builder = literal("set")
				.then(argument("targets", EntityArgument.entities())
					.then(literal("float")
						.then(argument("power", PowerArgument.power())
							.then(argument("value", FloatArgumentType.floatArg())
								.executes(SetArgument::setFloat))))
					.then(literal("int")
						.then(argument("power", PowerArgument.power())
							.then(argument("value", IntegerArgumentType.integer())
								.executes(SetArgument::setInt)))));

			return builder.build();

		}

		private static int setFloat(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

			CommandSourceStack commandSource = commandContext.getSource();

			List<Entity> targets = new ObjectArrayList<>(EntityArgument.getEntities(commandContext, "targets"));
			List<Entity> processedTargets = new ObjectArrayList<>();

			PowerHolder<?> powerHolder = PowerArgument.getPower(commandContext, "power");
			float value = FloatArgumentType.getFloat(commandContext, "value");

			if (!(powerHolder.value() instanceof FloatResourcePower)) {
				throw INVALID_FLOAT_POWER.create(powerHolder.id());
			}

			for (var target : targets) {

				Power.Instance<?> instance = Powers.getOptional(target)
					.flatMap(powers -> powers.getOptionalInstance(powerHolder.id()))
					.orElse(null);

				if (instance instanceof FloatResourcePower.Instance floatResource && floatResource.setValue(target, value)) {
					floatResource.syncData(target);
					processedTargets.add(target);
				}

			}

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendFailure(Component.translatableEscape("commands.neo-apoli.resource.set.float.fail.single", powerHolder.id(), targets.getFirst().getName()));
				}

				else {
					commandSource.sendFailure(Component.translatableEscape("commands.neo-apoli.resource.set.float.fail.multiple", powerHolder.id(), targets.size()));
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendSuccess(() -> Component.translatableEscape("commands.neo-apoli.resource.set.float.success.single", processedTargets.getFirst().getName(), powerHolder.id(), value), true);
				}

				else {
					commandSource.sendSuccess(() -> Component.translatableEscape("commands.neo-apoli.resource.set.float.success.multiple", processedTargets.size(), powerHolder.id(), value), true);
				}

			}

			return (int) (value * processedTargets.size());

		}

		private static int setInt(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

			CommandSourceStack commandSource = commandContext.getSource();

			List<Entity> targets = new ObjectArrayList<>(EntityArgument.getEntities(commandContext, "targets"));
			List<Entity> processedTargets = new ObjectArrayList<>();

			PowerHolder<?> powerHolder = PowerArgument.getPower(commandContext, "power");
			int value = IntegerArgumentType.getInteger(commandContext, "value");

			if (!(powerHolder.value() instanceof IntResourcePower)) {
				throw INVALID_INT_POWER.create(powerHolder.id());
			}

			for (var target : targets) {

				Power.Instance<?> instance = Powers.getOptional(target)
					.flatMap(powers -> powers.getOptionalInstance(powerHolder.id()))
					.orElse(null);

				if (instance instanceof IntResourcePower.Instance intResource && intResource.setValue(target, value)) {
					intResource.syncData(target);
					processedTargets.add(target);
				}

			}

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendFailure(Component.translatableEscape("commands.neo-apoli.resource.set.int.fail.single", powerHolder.id(), targets.getFirst().getName()));
				}

				else {
					commandSource.sendFailure(Component.translatableEscape("commands.neo-apoli.resource.set.int.fail.multiple", powerHolder.id(), targets.size()));
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendSuccess(() -> Component.translatableEscape("commands.neo-apoli.resource.set.int.success.single", processedTargets.getFirst().getName(), powerHolder.id(), value), true);
				}

				else {
					commandSource.sendSuccess(() -> Component.translatableEscape("commands.neo-apoli.resource.set.int.success.multiple", processedTargets.size(), powerHolder.id(), value), true);
				}

			}

			return value * processedTargets.size();

		}

	}

}
