package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.BoxContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextBoxProvider(BoxContextParameter parameter) implements BoxProvider {

	public static final MapCodec<ContextBoxProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParameterTypes.BOX.codec().fieldOf("parameter").forGetter(ContextBoxProvider::parameter))
		.apply(instance, ContextBoxProvider::new)
	);

	public static final Codec<ContextBoxProvider> INLINE_CODEC = NeoApoliContextParameterTypes.BOX.codec().xmap(
		ContextBoxProvider::new,
		ContextBoxProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextBoxProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParameterTypes.BOX.streamCodec(), ContextBoxProvider::parameter,
		ContextBoxProvider::new
	);

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.CONTEXT;
	}

	@Override
	public Optional<AABB> getValue(Context context) {

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
