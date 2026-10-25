package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record QuotientFloatProvider(FloatProvider dividend, FloatProvider divisor) implements FloatProvider {

	public static final MapCodec<QuotientFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("dividend").forGetter(QuotientFloatProvider::dividend),
		FloatProvider.CODEC.fieldOf("divisor").forGetter(QuotientFloatProvider::divisor)
	).apply(instance, QuotientFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, QuotientFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, QuotientFloatProvider::dividend,
		FloatProvider.STREAM_CODEC, QuotientFloatProvider::divisor,
		QuotientFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.QUOTIENT;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		Context dividendContext = context.forChild(".dividend");
		float dividend = dividend().getFloat(dividendContext);

		if (dividendContext.hasProblems()) {
			return Optional.empty();
		}

		Context divisorContext = context.forChild(".divisor");
		float divisor = divisor().getFloat(divisorContext);

		return divisorContext.hasProblems()
			? Optional.empty()
			: Optional.of(dividend / divisor);

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		dividend().validate(validator.forChild(".dividend"));
		divisor().validate(validator.forChild(".divisor"));
	}

}
