package io.github.eggohito.kaya.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.command_source.CommandSourceProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.provider.custom.string.StringProvider;
import io.github.eggohito.kaya.registry.provider.KayaIntProviderTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record CommandResultIntProvider(CommandSourceProvider source, StringProvider command) implements IntProvider {

	public static final MapCodec<CommandResultIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		CommandSourceProvider.CODEC.fieldOf("source").forGetter(CommandResultIntProvider::source),
		StringProvider.CODEC.fieldOf("command").forGetter(CommandResultIntProvider::command)
	).apply(instance, CommandResultIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CommandResultIntProvider> STREAM_CODEC = StreamCodec.composite(
		CommandSourceProvider.STREAM_CODEC, CommandResultIntProvider::source,
		StringProvider.STREAM_CODEC, CommandResultIntProvider::command,
		CommandResultIntProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaIntProviderTypes.COMMAND_RESULT;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return source().getValue(context.forChild(".source"))
			.flatMap(source -> command().getValue(context.forChild(".command"))
				.map(command -> this.execute(source, command)));
	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		source().validate(validator.forChild(".source"));
		command().validate(validator.forChild(".command"));
	}

	private int execute(CommandSourceStack source, String command) {

		MutableInt mutableResult = new MutableInt();
		source.getServer().getCommands().performPrefixedCommand(source.withCallback((success, result) -> mutableResult.setValue(result)), command);

		return mutableResult.intValue();

	}

}
