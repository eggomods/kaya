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

public record ArcTangent2FloatProvider(FloatProvider y, FloatProvider x) implements FloatProvider {

	public static final MapCodec<ArcTangent2FloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("y").forGetter(ArcTangent2FloatProvider::y),
		FloatProvider.CODEC.fieldOf("x").forGetter(ArcTangent2FloatProvider::x)
	).apply(instance, ArcTangent2FloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ArcTangent2FloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, ArcTangent2FloatProvider::y,
		FloatProvider.STREAM_CODEC, ArcTangent2FloatProvider::x,
		ArcTangent2FloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.ARC_TANGENT_2;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return y().getValue(context.forChild(".y"))
			.flatMap(y -> x().getValue(context.forChild(".x"))
				.map(x -> Math.atan2(y, x)));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		y().validate(validator.forChild(".y"));
		x().validate(validator.forChild(".x"));
	}

}
