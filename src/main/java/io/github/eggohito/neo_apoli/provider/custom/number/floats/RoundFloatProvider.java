package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import java.util.Optional;

public record RoundFloatProvider(FloatProvider value, IntProvider places, RoundingMode mode) implements FloatProvider {

	public static final MapCodec<RoundFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("value").forGetter(RoundFloatProvider::value),
		IntProvider.CODEC.fieldOf("places").forGetter(RoundFloatProvider::places),
		NeoApoliCodecs.ROUNDING_MODE.optionalFieldOf("mode", RoundingMode.HALF_UP).forGetter(RoundFloatProvider::mode)
	).apply(instance, RoundFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RoundFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, RoundFloatProvider::value,
		IntProvider.STREAM_CODEC, RoundFloatProvider::places,
		NeoApoliStreamCodecs.ROUNDING_MODE, RoundFloatProvider::mode,
		RoundFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.ROUND;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		Context valueContext = context.forChild(".value");
		float value = value().getFloat(valueContext);

		if (valueContext.hasProblems()) {
			return Optional.empty();
		}

		int places = places().getInt(context.forChild(".places"));
		StringBuilder pattern = new StringBuilder("#");

		if (places > 0) {
			pattern.append(".").repeat("#", places);
		}

		DecimalFormat formatter = new DecimalFormat(pattern.toString(), DecimalFormatSymbols.getInstance(Locale.ROOT));
		formatter.setRoundingMode(mode());

		try {
			return Optional.of(formatter.parse(formatter.format(value)).floatValue());
		}

		catch (ArithmeticException | ParseException e) {
			context.reportProblem(e.getMessage());
		}

		return Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
		places().validate(validator.forChild(".places"));
	}

}
