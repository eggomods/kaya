package io.github.eggohito.neo_apoli.provider.custom.bool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.BooleanContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBooleanProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextBooleanProvider(BooleanContextParameter parameter) implements BooleanProvider {

	public static final MapCodec<ContextBooleanProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParameterTypes.BOOLEAN.codec().fieldOf("parameter").forGetter(ContextBooleanProvider::parameter))
		.apply(instance, ContextBooleanProvider::new)
	);

	public static final Codec<ContextBooleanProvider> INLINE_CODEC = NeoApoliContextParameterTypes.BOOLEAN.codec().xmap(
		ContextBooleanProvider::new,
		ContextBooleanProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextBooleanProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParameterTypes.BOOLEAN.streamCodec(), ContextBooleanProvider::parameter,
		ContextBooleanProvider::new
	);

	@Override
	public @NotNull BooleanProvider.Type<?> getType() {
		return NeoApoliBooleanProviderTypes.CONTEXT;
	}

	@Override
	public Optional<Boolean> getValue(Context context) {

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
