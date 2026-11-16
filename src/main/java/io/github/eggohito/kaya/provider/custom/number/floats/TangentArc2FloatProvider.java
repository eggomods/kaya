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

public record TangentArc2FloatProvider(FloatProvider y, FloatProvider x) implements FloatProvider {

	public static final MapCodec<TangentArc2FloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("y").forGetter(TangentArc2FloatProvider::y),
		FloatProvider.CODEC.fieldOf("x").forGetter(TangentArc2FloatProvider::x)
	).apply(instance, TangentArc2FloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TangentArc2FloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, TangentArc2FloatProvider::y,
		FloatProvider.STREAM_CODEC, TangentArc2FloatProvider::x,
		TangentArc2FloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.TANGENT_ARC_2;
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
