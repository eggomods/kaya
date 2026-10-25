package io.github.eggohito.neo_apoli.provider.custom.slot;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliSlotProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import org.jetbrains.annotations.NotNull;

public record ConditionalSlotProvider(Condition condition, SlotProvider onTrue, SlotProvider onFalse) implements SlotProvider, ConditionalValueProvider<SlotAccess, SlotProvider> {

	public static final MapCodec<ConditionalSlotProvider> CODEC = ConditionalValueProvider.mapCodec(SlotProvider.CODEC, ConditionalSlotProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalSlotProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(SlotProvider.STREAM_CODEC, ConditionalSlotProvider::new);

	@Override
	public SlotProvider.@NotNull Type<?> getType() {
		return NeoApoliSlotProviderTypes.CONDITIONAL;
	}

}
