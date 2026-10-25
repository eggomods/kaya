package io.github.eggohito.neo_apoli.provider.custom.item;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliItemProviderTypes;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalItemProvider(List<CompositeConditional.Entry<ItemProvider>> entries, ItemProvider defaultValue) implements ItemProvider, CompositeConditionalValueProvider<ItemStack, ItemProvider> {

	public static final MapCodec<CompositeConditionalItemProvider> CODEC = CompositeConditionalValueProvider.mapCodec(ItemProvider.CODEC, CompositeConditionalItemProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalItemProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(ItemProvider.STREAM_CODEC, CompositeConditionalItemProvider::new);

	@Override
	public ItemProvider.@NotNull Type<?> getType() {
		return NeoApoliItemProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
