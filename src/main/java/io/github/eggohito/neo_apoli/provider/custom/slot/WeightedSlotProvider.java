package io.github.eggohito.neo_apoli.provider.custom.slot;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliSlotProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.SlotAccess;
import org.jetbrains.annotations.NotNull;

public record WeightedSlotProvider(WeightedList<SlotProvider> entries) implements SlotProvider, WeightedValueProvider<SlotAccess, SlotProvider> {

	public static final MapCodec<WeightedSlotProvider> CODEC = WeightedValueProvider.mapCodec(SlotProvider.CODEC, WeightedSlotProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedSlotProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(SlotProvider.STREAM_CODEC, WeightedSlotProvider::new);

	@Override
	public SlotProvider.@NotNull Type<?> getType() {
		return NeoApoliSlotProviderTypes.WEIGHTED;
	}

}
