package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record QuotientIntProvider(IntProvider dividend, IntProvider divisor) implements IntProvider {

	public static final MapCodec<QuotientIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.fieldOf("dividend").forGetter(QuotientIntProvider::dividend),
		IntProvider.CODEC.fieldOf("divisor").forGetter(QuotientIntProvider::divisor)
	).apply(instance, QuotientIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, QuotientIntProvider> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, QuotientIntProvider::dividend,
		IntProvider.STREAM_CODEC, QuotientIntProvider::divisor,
		QuotientIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.QUOTIENT;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Context dividendContext = context.forChild(".dividend");
		int dividend = dividend().getInt(dividendContext);

		if (dividendContext.hasProblems()) {
			return Optional.empty();
		}

		Context divisorContext = context.forChild(".divisor");
		int divisor = divisor().getInt(divisorContext);

		if (divisorContext.hasProblems()) {
			return Optional.empty();
		}

		try {
			return Optional.of(dividend / divisor);
		}

		catch (ArithmeticException e) {
			context.reportProblem(e.getMessage());
		}

		return Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		dividend().validate(validator.forChild(".dividend"));
		divisor().validate(validator.forChild(".divisor"));
	}

}
