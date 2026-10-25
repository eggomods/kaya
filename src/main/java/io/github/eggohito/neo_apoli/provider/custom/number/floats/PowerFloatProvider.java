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

public record PowerFloatProvider(FloatProvider base, FloatProvider exponent) implements FloatProvider {

	public static final MapCodec<PowerFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("base").forGetter(PowerFloatProvider::base),
		FloatProvider.CODEC.fieldOf("exponent").forGetter(PowerFloatProvider::exponent)
	).apply(instance, PowerFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PowerFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, PowerFloatProvider::base,
		FloatProvider.STREAM_CODEC, PowerFloatProvider::exponent,
		PowerFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.POWER;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return base().getValue(context.forChild(".base"))
			.flatMap(base -> exponent().getValue(context.forChild(".exponent"))
				.map(exponent -> (float) Math.pow(base, exponent)));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		base().validate(validator.forChild(".base"));
		exponent().validate(validator.forChild(".exponent"));
	}

}
