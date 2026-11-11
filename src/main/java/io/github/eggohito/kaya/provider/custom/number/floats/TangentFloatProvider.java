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

public record TangentFloatProvider(FloatProvider radian) implements FloatProvider {

	public static final MapCodec<TangentFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("radian").forGetter(TangentFloatProvider::radian))
		.apply(instance, TangentFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, TangentFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, TangentFloatProvider::radian,
		TangentFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.TANGENT;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return radian().getValue(context.forChild(".radian")).map(Math::tan);
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		radian().validate(validator.forChild(".radian"));
	}

}
