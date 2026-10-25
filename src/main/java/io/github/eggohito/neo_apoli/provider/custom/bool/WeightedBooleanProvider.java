package io.github.eggohito.neo_apoli.provider.custom.bool;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBooleanProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

public record WeightedBooleanProvider(WeightedList<BooleanProvider> entries) implements BooleanProvider, WeightedValueProvider<Boolean, BooleanProvider> {

	public static final MapCodec<WeightedBooleanProvider> CODEC = WeightedValueProvider.mapCodec(BooleanProvider.CODEC, WeightedBooleanProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedBooleanProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(BooleanProvider.STREAM_CODEC, WeightedBooleanProvider::new);

	@Override
	public @NotNull BooleanProvider.Type<?> getType() {
		return NeoApoliBooleanProviderTypes.WEIGHTED;
	}

}
