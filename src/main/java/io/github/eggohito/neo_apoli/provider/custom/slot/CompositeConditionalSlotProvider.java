package io.github.eggohito.neo_apoli.provider.custom.slot;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliSlotProviderTypes;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalSlotProvider(List<CompositeConditional.Entry<SlotProvider>> entries, SlotProvider defaultValue) implements SlotProvider, CompositeConditionalValueProvider<SlotAccess, SlotProvider> {

	public static final MapCodec<CompositeConditionalSlotProvider> CODEC = CompositeConditionalValueProvider.mapCodec(SlotProvider.CODEC, CompositeConditionalSlotProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalSlotProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(SlotProvider.STREAM_CODEC, CompositeConditionalSlotProvider::new);

	@Override
	public SlotProvider.@NotNull Type<?> getType() {
		return NeoApoliSlotProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
