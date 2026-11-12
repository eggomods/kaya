package io.github.eggohito.kaya.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.registry.provider.KayaFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.Optional;

public record ArcSineFloatProvider(FloatProvider value) implements FloatProvider {

	public static final MapCodec<ArcSineFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("value").forGetter(ArcSineFloatProvider::value))
		.apply(instance, ArcSineFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ArcSineFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, ArcSineFloatProvider::value,
		ArcSineFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.ARC_SINE;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return value().getValue(context.forChild(".value")).map(Math::asin);
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
	}

}
