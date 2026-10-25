package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalFloatProvider(List<Entry<FloatProvider>> entries, FloatProvider defaultValue) implements FloatProvider, CompositeConditionalValueProvider<Float, FloatProvider> {

	public static final MapCodec<CompositeConditionalFloatProvider> CODEC = CompositeConditionalValueProvider.mapCodec(FloatProvider.CODEC, CompositeConditionalFloatProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalFloatProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(FloatProvider.STREAM_CODEC, CompositeConditionalFloatProvider::new);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
