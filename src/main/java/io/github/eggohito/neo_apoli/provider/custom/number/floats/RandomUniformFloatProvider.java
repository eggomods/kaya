package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RandomUniformFloatProvider(FloatProvider min, FloatProvider max) implements FloatProvider {

	public static final MapCodec<RandomUniformFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("min").forGetter(RandomUniformFloatProvider::min),
		FloatProvider.CODEC.fieldOf("max").forGetter(RandomUniformFloatProvider::max)
	).apply(instance, RandomUniformFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RandomUniformFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, RandomUniformFloatProvider::min,
		FloatProvider.STREAM_CODEC, RandomUniformFloatProvider::max,
		RandomUniformFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.RANDOM_UNIFORM;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		Context minContext = context.forChild(".min");
		float min = min().getFloat(minContext);

		if (minContext.hasProblems()) {
			return Optional.empty();
		}

		Context maxContext = context.forChild(".max");
		float max = max().getFloat(maxContext);

		if (maxContext.hasProblems()) {
			return Optional.of(min);
		}

		else {
			return Optional.of(Mth.nextFloat(context.level().getRandom(), min, max));
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		min().validate(validator.forChild(".min"));
		max().validate(validator.forChild(".max"));
	}

}
