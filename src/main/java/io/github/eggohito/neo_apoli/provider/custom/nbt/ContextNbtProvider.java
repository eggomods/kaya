package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.NbtContextParameter;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextNbtProvider(NbtContextParameter parameter) implements NbtProvider {

	public static final MapCodec<ContextNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParameterTypes.NBT.codec().fieldOf("parameter").forGetter(ContextNbtProvider::parameter))
		.apply(instance, ContextNbtProvider::new)
	);

	public static final Codec<ContextNbtProvider> INLINE_CODEC = NeoApoliContextParameterTypes.NBT.codec().xmap(
		ContextNbtProvider::new,
		ContextNbtProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextNbtProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParameterTypes.NBT.streamCodec(), ContextNbtProvider::parameter,
		ContextNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.CONTEXT;
	}

	@Override
	public Optional<Tag> getValue(Context context) {

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
