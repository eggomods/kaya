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

public record NegateIntProvider(IntProvider value) implements IntProvider {

	public static final MapCodec<NegateIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(IntProvider.CODEC.fieldOf("value").forGetter(NegateIntProvider::value))
		.apply(instance, NegateIntProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, NegateIntProvider> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, NegateIntProvider::value,
		NegateIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.NEGATE;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		Context valueContext = context.forChild(".value");
		return value()
			.getValue(valueContext)
			.flatMap(value -> this.negate(context, value));
	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
	}

	private Optional<Integer> negate(Context context, int value) {

		try {
			return Optional.of(Math.negateExact(value));
		}

		catch (ArithmeticException e) {
			context.reportProblem(e.getMessage());
		}

		return Optional.empty();

	}

}
