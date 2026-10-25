package io.github.eggohito.neo_apoli.provider.custom.item;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliItemProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record WeightedItemProvider(WeightedList<ItemProvider> entries) implements ItemProvider, WeightedValueProvider<ItemStack, ItemProvider> {

	public static final MapCodec<WeightedItemProvider> CODEC = WeightedValueProvider.mapCodec(ItemProvider.CODEC, WeightedItemProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedItemProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(ItemProvider.STREAM_CODEC, WeightedItemProvider::new);

	@Override
	public ItemProvider.@NotNull Type<?> getType() {
		return NeoApoliItemProviderTypes.WEIGHTED;
	}

}
