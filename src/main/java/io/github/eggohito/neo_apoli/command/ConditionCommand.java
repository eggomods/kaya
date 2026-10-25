package io.github.eggohito.neo_apoli.command;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.neo_apoli.command.argument.ConditionArgument;
import io.github.eggohito.neo_apoli.command.argument.ContextParametersArgument;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.manager.ConditionManager;
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

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ConditionCommand {

	public static final ContextValidator.Parameters COMMAND_PARAMETERS = new ContextValidator.Parameters.Builder()
		.optional(NeoApoliContextParameters.COMMAND_ENTITY)
		.required(NeoApoliContextParameters.COMMAND_POSITION)
		.build();

	public static void register(CommandBuildContext buildContext, CommandNode<CommandSourceStack> rootNode) {

		var baseNode = literal("condition")
			.requires(source -> source.hasPermission(2))
			.build();

		baseNode.addChild(Dump.node(buildContext));
		baseNode.addChild(Test.node(buildContext));

		rootNode.addChild(baseNode);

	}

	public static final class Dump {

		public static CommandNode<CommandSourceStack> node(CommandBuildContext buildContext) {

			var node = literal("dump")
				.then(argument("condition", ConditionArgument.condition(buildContext))
					.executes(Dump::withDefaultIndent)
					.then(argument("indent", IntegerArgumentType.integer(0))
						.executes(Dump::withSpecificIndent)));

			return node.build();

		}

		public static int withDefaultIndent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			return execute(context, 4);
		}

		public static int withSpecificIndent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			return execute(context, IntegerArgumentType.getInteger(context, "indent"));
		}

		public static int execute(CommandContext<CommandSourceStack> context, int indent) throws CommandSyntaxException {

			CommandSourceStack source = context.getSource();
			Condition condition = ConditionArgument.getCondition(context, "condition");

			return switch (Condition.CODEC.encodeStart(source.registryAccess().createSerializationContext(JsonOps.INSTANCE), condition)) {
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

	public static final class Test {

		public static CommandNode<CommandSourceStack> node(CommandBuildContext buildContext) {

			var testNode = literal("test")
				.then(argument("condition", ConditionArgument.inlineCondition(buildContext))
					.then(literal("with")
						.then(argument("parameters", ContextParametersArgument.parameters(buildContext))
							.executes(Test::testAsInt))));

			return testNode.build();

		}

		public static int testAsInt(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

			if (test(commandContext)) {
				commandContext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
			}

			else {
				throw MiscUtil.createCommandException(Component.translatable("commands.execute.conditional.fail"));
			}

			return 1;

		}

		public static boolean test(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

			CommandSourceStack source = commandContext.getSource();
			ContextParameterMap parameters = ContextParametersArgument.getParameters(commandContext, "parameters");

			Condition condition = ConditionArgument.getCondition(commandContext, "condition");
			String path = ConditionManager.getInstance().getKeyAsResult(condition).mapOrElse(id -> "{\"" + id + "\"}", error -> "{type: \"" + Util.getRegisteredName(NeoApoliRegistries.CONDITION_TYPE, condition.getType()) + "\"}");

			Reporter reporter = new Reporter(path);
			ContextValidator validator = new ContextValidator(COMMAND_PARAMETERS.merge(parameters.forValidation()), reporter).withResolver(source.registryAccess());

			condition.validate(validator);

			if (reporter.hasProblems()) {
				throw MiscUtil.createCommandException(Component.literal("Found errors while validating the condition\n" + reporter.getReport()));
			}

			Context baseContext = new Context.Builder()
				.withReporter(reporter)
				.withNullable(NeoApoliContextParameters.COMMAND_ENTITY, source.getEntity())
				.withRequired(NeoApoliContextParameters.COMMAND_POSITION, source.getPosition())
				.build(source.getLevel());

			Context context = parameters.forUser(baseContext, ctx -> ctx.forChild(".parameters"));
			boolean result = condition.test(context);

			if (reporter.hasProblems()) {
				throw MiscUtil.createCommandException(Component.literal("Found errors while testing the condition\n" + reporter.getReport()));
			}

			return result;

		}

	}

}
