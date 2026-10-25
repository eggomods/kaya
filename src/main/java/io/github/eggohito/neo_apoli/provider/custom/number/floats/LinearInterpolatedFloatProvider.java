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

public record LinearInterpolatedFloatProvider(FloatProvider delta, FloatProvider start, FloatProvider end) implements FloatProvider {

	public static final MapCodec<LinearInterpolatedFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("delta").forGetter(LinearInterpolatedFloatProvider::delta),
		FloatProvider.CODEC.fieldOf("start").forGetter(LinearInterpolatedFloatProvider::start),
		FloatProvider.CODEC.fieldOf("end").forGetter(LinearInterpolatedFloatProvider::end)
	).apply(instance, LinearInterpolatedFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LinearInterpolatedFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, LinearInterpolatedFloatProvider::delta,
		FloatProvider.STREAM_CODEC, LinearInterpolatedFloatProvider::start,
		FloatProvider.STREAM_CODEC, LinearInterpolatedFloatProvider::end,
		LinearInterpolatedFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.LINEAR_INTERPOLATED;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		Context startContext = context.forChild(".start");
		float start = start().getFloat(startContext);

		if (startContext.hasProblems()) {
			return Optional.empty();
		}

		Context deltaContext = context.forChild(".delta");
		float delta = delta().getFloat(deltaContext);

		if (deltaContext.hasProblems()) {
			return Optional.of(start);
		}

		else {

			Context endContext = context.forChild(".end");
			float end = end().getFloat(endContext);

			if (endContext.hasProblems()) {
				return Optional.of(start);
			}

			else {
				return Optional.of(Mth.clampedLerp(start, end, delta));
			}

		}

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		delta().validate(validator.forChild(".delta"));
		start().validate(validator.forChild(".start"));
		end().validate(validator.forChild(".end"));
	}

}
