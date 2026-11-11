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

public record SineFloatProvider(FloatProvider radian) implements FloatProvider {

	public static final MapCodec<SineFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("radian").forGetter(SineFloatProvider::radian))
		.apply(instance, SineFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, SineFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, SineFloatProvider::radian,
		SineFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.SINE;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return radian().getValue(context.forChild("radian")).map(Mth::sin);
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		radian().validate(validator.forChild(".radian"));
	}

}
