package io.github.eggohito.neo_apoli.provider.custom.item;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliItemProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ConditionalItemProvider(Condition condition, ItemProvider onTrue, ItemProvider onFalse) implements ItemProvider, ConditionalValueProvider<ItemStack, ItemProvider> {

	public static final MapCodec<ConditionalItemProvider> CODEC = ConditionalValueProvider.mapCodec(ItemProvider.CODEC, ConditionalItemProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalItemProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(ItemProvider.STREAM_CODEC, ConditionalItemProvider::new);

	@Override
	public ItemProvider.@NotNull Type<?> getType() {
		return NeoApoliItemProviderTypes.CONDITIONAL;
	}

}
