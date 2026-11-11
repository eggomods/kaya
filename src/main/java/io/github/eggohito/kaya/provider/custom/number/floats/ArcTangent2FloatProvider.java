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

public record ArcTangent2FloatProvider(FloatProvider ordinate, FloatProvider abscissa) implements FloatProvider {

	public static final MapCodec<ArcTangent2FloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("ordinate").forGetter(ArcTangent2FloatProvider::ordinate),
		FloatProvider.CODEC.fieldOf("abscissa").forGetter(ArcTangent2FloatProvider::abscissa)
	).apply(instance, ArcTangent2FloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ArcTangent2FloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, ArcTangent2FloatProvider::ordinate,
		FloatProvider.STREAM_CODEC, ArcTangent2FloatProvider::abscissa,
		ArcTangent2FloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.ARC_TANGENT_2;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return ordinate().getValue(context.forChild(".ordinate"))
			.flatMap(y -> abscissa().getValue(context.forChild(".abscissa"))
				.map(x -> Math.atan2(y, x)));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		ordinate().validate(validator.forChild(".ordinate"));
		abscissa().validate(validator.forChild(".abscissa"));
	}

}
