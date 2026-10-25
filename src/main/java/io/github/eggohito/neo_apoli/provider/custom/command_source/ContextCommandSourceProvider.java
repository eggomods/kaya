package io.github.eggohito.neo_apoli.provider.custom.command_source;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.CommandSourceContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliCommandSourceProviderTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextCommandSourceProvider(CommandSourceContextParameter parameter) implements CommandSourceProvider {

	public static final MapCodec<ContextCommandSourceProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParameterTypes.COMMAND_SOURCE.codec().fieldOf("parameter").forGetter(ContextCommandSourceProvider::parameter))
		.apply(instance, ContextCommandSourceProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextCommandSourceProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParameterTypes.COMMAND_SOURCE.streamCodec(), ContextCommandSourceProvider::parameter,
		ContextCommandSourceProvider::new
	);

	@Override
	public CommandSourceProvider.@NotNull Type<?> getType() {
		return NeoApoliCommandSourceProviderTypes.CONTEXT;
	}

	@Override
	public Optional<CommandSourceStack> getValue(Context context) {

		if (!context.hasParameter(parameter())) {
			context.reportProblem("Parameter \"" + parameter.name() + "\" is not provided in the context!");
		}

		return context.getOptional(parameter());

	}

	@Override
	public Set<ContextParameter<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
