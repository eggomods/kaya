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

public record ToDegreesFloatProvider(FloatProvider radians) implements FloatProvider {

	public static final MapCodec<ToDegreesFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("radians").forGetter(ToDegreesFloatProvider::radians))
		.apply(instance, ToDegreesFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ToDegreesFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, ToDegreesFloatProvider::radians,
		ToDegreesFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.TO_DEGREES;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return radians().getValue(context.forChild(".radians")).map(Math::toDegrees);
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		radians().validate(validator.forChild(".radians"));
	}

}
