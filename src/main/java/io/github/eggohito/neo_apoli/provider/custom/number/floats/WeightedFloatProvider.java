package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

public record WeightedFloatProvider(WeightedList<FloatProvider> entries) implements FloatProvider, WeightedValueProvider<Float, FloatProvider> {

	public static final MapCodec<WeightedFloatProvider> CODEC = WeightedValueProvider.mapCodec(FloatProvider.CODEC, WeightedFloatProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedFloatProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(FloatProvider.STREAM_CODEC, WeightedFloatProvider::new);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.WEIGHTED;
	}

}
