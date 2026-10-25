package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ClampedIntProvider(IntProvider value, IntProvider min, IntProvider max) implements IntProvider {

	public static final MapCodec<ClampedIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.fieldOf("value").forGetter(ClampedIntProvider::value),
		IntProvider.CODEC.fieldOf("min").forGetter(ClampedIntProvider::min),
		IntProvider.CODEC.fieldOf("max").forGetter(ClampedIntProvider::max)
	).apply(instance, ClampedIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClampedIntProvider> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, ClampedIntProvider::value,
		IntProvider.STREAM_CODEC, ClampedIntProvider::min,
		IntProvider.STREAM_CODEC, ClampedIntProvider::max,
		ClampedIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.CLAMPED;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Context valueContext = context.forChild(".value");
		int value = value().getInt(valueContext);

		if (valueContext.hasProblems()) {
			return Optional.empty();
		}

		Context minContext = context.forChild(".min");
		int min = min().getInt(minContext);

		if (minContext.hasProblems()) {
			return Optional.of(value);
		}

		else {

			Context maxContext = context.forChild(".max");
			int max = max().getInt(maxContext);

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
		IntProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
		min().validate(validator.forChild(".min"));
		max().validate(validator.forChild(".max"));
	}

}
