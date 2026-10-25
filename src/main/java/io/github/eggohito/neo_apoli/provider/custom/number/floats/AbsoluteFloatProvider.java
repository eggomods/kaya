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

public record AbsoluteFloatProvider(FloatProvider value) implements FloatProvider {

	public static final MapCodec<AbsoluteFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.CODEC.fieldOf("value").forGetter(AbsoluteFloatProvider::value))
		.apply(instance, AbsoluteFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, AbsoluteFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, AbsoluteFloatProvider::value,
		AbsoluteFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.ABSOLUTE;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return value().getValue(context.forChild(".value")).map(Math::abs);
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		value().validate(validator.forChild(".value"));
	}

}
