package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RandomBinomialIntProvider(IntProvider attempts, FloatProvider probability) implements IntProvider {

	public static final MapCodec<RandomBinomialIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.fieldOf("attempts").forGetter(RandomBinomialIntProvider::attempts),
		FloatProvider.CODEC.fieldOf("probability").forGetter(RandomBinomialIntProvider::probability)
	).apply(instance, RandomBinomialIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RandomBinomialIntProvider> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, RandomBinomialIntProvider::attempts,
		FloatProvider.STREAM_CODEC, RandomBinomialIntProvider::probability,
		RandomBinomialIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.RANDOM_BINOMIAL;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Context attemptsContext = context.forChild(".attempts");
		int attempts = attempts().getInt(attemptsContext);

		if (attemptsContext.hasProblems()) {
			return Optional.empty();
		}

		Context probabilityContext = context.forChild(".probability");
		float probability = probability().getFloat(probabilityContext);

		if (probabilityContext.hasProblems()) {
			return Optional.empty();
		}

		RandomSource random = context.level().getRandom();
		int result = 0;

		for (int i = 0; i < attempts; ++i) {

			if (random.nextFloat() < probability) {
				result++;
			}

		}

		return Optional.of(result);

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		attempts().validate(validator.forChild(".attempts"));
		probability().validate(validator.forChild(".probability"));
	}

}
