package io.github.eggohito.neo_apoli.command;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.manager.ActionManager;
import io.github.eggohito.neo_apoli.command.argument.ActionArgument;
import io.github.eggohito.neo_apoli.command.argument.ContextParametersArgument;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextParameterMap;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.JsonTextFormatter;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.Reporter;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ActionCommand {

	public static final ContextValidator.Parameters COMMAND_PARAMETERS = new ContextValidator.Parameters.Builder()
		.optional(NeoApoliContextParameters.COMMAND_ENTITY)
		.required(NeoApoliContextParameters.COMMAND_POSITION)
		.build();

	public static void register(CommandBuildContext buildContext, CommandNode<CommandSourceStack> rootNode) {

		var baseNode = literal("action")
			.requires(source -> source.hasPermission(2))
			.build();

		baseNode.addChild(Dump.node(buildContext));
		baseNode.addChild(Execute.node(buildContext));

		rootNode.addChild(baseNode);

	}

	public static final class Dump {

		public static CommandNode<CommandSourceStack> node(CommandBuildContext buildContext) {

			var node = literal("dump")
				.then(argument("action", ActionArgument.id(buildContext))
					.executes(Dump::withDefaultIndent)
					.then(argument("indent", IntegerArgumentType.integer(0))
						.executes(Dump::withSpecificIndent)));

			return node.build();

		}

		private static int withDefaultIndent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			return execute(context, 4);
		}

		private static int withSpecificIndent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			return execute(context, IntegerArgumentType.getInteger(context, "indent"));
		}

		public static int execute(CommandContext<CommandSourceStack> context, int indent) throws CommandSyntaxException {

			CommandSourceStack source = context.getSource();
			Action action = ActionArgument.getActions(context, "action").getFirst();

			return switch (Action.CODEC.encodeStart(source.registryAccess().createSerializationContext(JsonOps.INSTANCE), action)) {
				case DataResult.Success<JsonElement> success -> {

					JsonElement jsonElement = success.value();
					source.sendSuccess(() -> JsonTextFormatter.format(jsonElement, indent), false);

					yield jsonElement.toString().length();

				}
				case DataResult.Error<JsonElement> error ->
					throw MiscUtil.createCommandException(error::message);
			};

		}

	}

	public static final class Execute {

		public static CommandNode<CommandSourceStack> node(CommandBuildContext buildContext) {

			var executeNode = literal("execute")
				.then(argument("action", ActionArgument.idOrTagOrInline(buildContext))
					.then(literal("with")
						.then(argument("parameters", ContextParametersArgument.parameters(buildContext))
							.executes(Execute::execute))));

			return executeNode.build();

		}

		public static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

			CommandSourceStack source = commandContext.getSource();
			ContextParameterMap parameters = ContextParametersArgument.getParameters(commandContext, "parameters");

			List<Action> actions = ActionArgument.getActions(commandContext, "action");
			int executed = 0;

			for (var action : actions) {

				String path = ActionManager.getInstance().getKeyAsResult(action).mapOrElse(id -> "{\"" + id + "\"}", ignored -> "{type: \"" + Util.getRegisteredName(NeoApoliRegistries.ACTION_TYPE, action.getType()) + "\"}");
				Reporter reporter = new Reporter(path);

				ContextValidator validator = new ContextValidator(COMMAND_PARAMETERS.merge(parameters.forValidation()), reporter).withResolver(source.registryAccess());
				action.validate(validator);

				if (reporter.hasProblems()) {
					throw MiscUtil.createCommandException(Component.literal("Found errors while validating the action\n" + reporter.getReport()));
				}

				Context baseContext = new Context.Builder()
					.withReporter(reporter)
					.withNullable(NeoApoliContextParameters.COMMAND_ENTITY, source.getEntity())
					.withRequired(NeoApoliContextParameters.COMMAND_POSITION, source.getPosition())
					.build(source.getLevel());

				Context context = parameters.forUser(baseContext, ctx -> ctx.forChild(".parameters"));
				action.execute(context);

				if (reporter.hasProblems()) {
					throw MiscUtil.createCommandException(Component.literal("Found errors while executing the action\n" + reporter.getReport()));
				}

				executed++;

			}

			commandContext.getSource().sendSuccess(() -> Component.literal("Successfully executed action!"), false);
			return executed;

		}

	}

}
