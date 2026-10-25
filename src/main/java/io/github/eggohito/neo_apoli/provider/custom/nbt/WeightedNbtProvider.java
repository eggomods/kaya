package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

public record WeightedNbtProvider(WeightedList<NbtProvider> entries) implements NbtProvider, WeightedValueProvider<Tag, NbtProvider> {

	public static final MapCodec<WeightedNbtProvider> CODEC = WeightedValueProvider.mapCodec(NbtProvider.CODEC, WeightedNbtProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedNbtProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(NbtProvider.STREAM_CODEC, WeightedNbtProvider::new);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.WEIGHTED;
	}

}
