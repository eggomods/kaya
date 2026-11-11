package io.github.eggohito.kaya.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.registry.provider.KayaFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record CosineFloatProvider(FloatProvider radian) implements FloatProvider {

	public static final MapCodec<CosineFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("radian").forGetter(CosineFloatProvider::radian))
		.apply(instance, CosineFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, CosineFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, CosineFloatProvider::radian,
		CosineFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.COSINE;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return radian().getValue(context.forChild(".radian")).map(Mth::cos);
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		radian().validate(validator.forChild(".radian"));
	}

}
