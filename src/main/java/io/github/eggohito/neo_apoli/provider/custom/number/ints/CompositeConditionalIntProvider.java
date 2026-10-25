package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalIntProvider(List<Entry<IntProvider>> entries, IntProvider defaultValue) implements IntProvider, CompositeConditionalValueProvider<Integer, IntProvider> {

	public static final MapCodec<CompositeConditionalIntProvider> CODEC = CompositeConditionalValueProvider.mapCodec(IntProvider.CODEC, CompositeConditionalIntProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalIntProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(IntProvider.STREAM_CODEC, CompositeConditionalIntProvider::new);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
