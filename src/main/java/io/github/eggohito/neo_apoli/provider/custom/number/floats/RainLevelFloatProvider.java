package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.mixin.access.LevelAccessor;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RainLevelFloatProvider(FloatProvider delta) implements FloatProvider {

	public static final MapCodec<RainLevelFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(FloatProvider.clamped(0.0F, 1.0F).optionalFieldOf("delta", new ConstantFloatProvider(1.0F)).forGetter(RainLevelFloatProvider::delta))
		.apply(instance, RainLevelFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, RainLevelFloatProvider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, RainLevelFloatProvider::delta,
		RainLevelFloatProvider::new
	);

	public RainLevelFloatProvider {

		if (!(delta instanceof ClampedFloatProvider)) {
			delta = new ClampedFloatProvider(delta, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

	}

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliFloatProviderTypes.RAIN_LEVEL;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		if (((LevelAccessor) context.level()).callCanHaveWeather()) {
			return delta().getValue(context.forChild(".delta")).map(delta -> context.level().getRainLevel(delta));
		}

		else {
			return Optional.empty();
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		delta().validate(validator.forChild(".delta"));
	}

}
