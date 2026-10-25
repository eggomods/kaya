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

public record ClampedFloatProvider(FloatProvider value, FloatProvider min, FloatProvider max) implements FloatProvider {

	public static final MapCodec<ClampedFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("value").forGetter(ClampedFloatProvider::value),
		FloatProvider.CODEC.fieldOf("min").forGetter(ClampedFloatProvider::min),
		FloatProvider.CODEC.fieldOf("max").forGetter(ClampedFloatProvider::max)
	).apply(instance, ClampedFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClampedFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, ClampedFloatProvider::value,
		FloatProvider.STREAM_CODEC, ClampedFloatProvider::min,
		FloatProvider.STREAM_CODEC, ClampedFloatProvider::max,
		ClampedFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.CLAMPED;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		Context valueContext = context.forChild(".value");
		float value = value().getFloat(valueContext);

		if (valueContext.hasProblems()) {
			return Optional.empty();
		}

		Context minContext = context.forChild(".min");
		float min = min().getFloat(minContext);

		if (minContext.hasProblems()) {
			return Optional.of(value);
		}

		else {

			Context maxContext = context.forChild(".max");
			float max = max().getFloat(maxContext);

			if (maxContext.hasProblems()) {
				return Optional.of(Math.max(value, min));
			}

			else {
				return Optional.of(Mth.clamp(value, min, max));
			}

		}

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
		min().validate(validator.forChild(".min"));
		max().validate(validator.forChild(".max"));
	}

}
