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

public record ToRadiansFloatProvider(FloatProvider degrees) implements FloatProvider {

	public static final MapCodec<ToRadiansFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("degrees").forGetter(ToRadiansFloatProvider::degrees))
		.apply(instance, ToRadiansFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ToRadiansFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, ToRadiansFloatProvider::degrees,
		ToRadiansFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.TO_RADIANS;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return degrees().getValue(context.forChild(".degrees")).map(Math::toRadians);
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		degrees().validate(validator.forChild(".degrees"));
	}

}
