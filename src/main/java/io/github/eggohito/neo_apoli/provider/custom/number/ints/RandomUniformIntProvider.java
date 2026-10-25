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

public record RandomUniformIntProvider(IntProvider min, IntProvider max) implements IntProvider {

	public static final MapCodec<RandomUniformIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.fieldOf("min").forGetter(RandomUniformIntProvider::min),
		IntProvider.CODEC.fieldOf("max").forGetter(RandomUniformIntProvider::max)
	).apply(instance, RandomUniformIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RandomUniformIntProvider> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, RandomUniformIntProvider::min,
		IntProvider.STREAM_CODEC, RandomUniformIntProvider::max,
		RandomUniformIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.RANDOM_UNIFORM;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Context minContext = context.forChild(".min");
		int min = min().getInt(minContext);

		if (minContext.hasProblems()) {
			return Optional.empty();
		}

		Context maxContext = context.forChild(".max");
		int max = max().getInt(maxContext);

		if (maxContext.hasProblems()) {
			return Optional.of(Mth.nextInt(context.level().getRandom(), min, Integer.MAX_VALUE));
		}

		else {
			return Optional.of(Mth.nextInt(context.level().getRandom(), min, max));
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		min().validate(validator.forChild(".min"));
		max().validate(validator.forChild(".max"));
	}

}
