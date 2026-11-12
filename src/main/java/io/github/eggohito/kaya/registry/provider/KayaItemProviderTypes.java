package io.github.eggohito.kaya.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.provider.custom.item.*;
import io.github.eggohito.kaya.registry.KayaRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class KayaItemProviderTypes {

	public static final ItemProvider.Type<CompositeConditionalItemProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalItemProvider.CODEC, CompositeConditionalItemProvider.STREAM_CODEC);
	public static final ItemProvider.Type<ConditionalItemProvider> CONDITIONAL = registerInternal("conditional", ConditionalItemProvider.CODEC, ConditionalItemProvider.STREAM_CODEC);
	public static final ItemProvider.Type<ContextItemProvider> CONTEXT = registerInternal("context", ContextItemProvider.CODEC, ContextItemProvider.STREAM_CODEC);
	public static final ItemProvider.Type<WeightedItemProvider> WEIGHTED = registerInternal("weighted", WeightedItemProvider.CODEC, WeightedItemProvider.STREAM_CODEC);

	public static final ItemProvider.Type<FromSlotItemProvider> FROM_SLOT = registerInternal("from_slot", FromSlotItemProvider.CODEC, FromSlotItemProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <P extends ItemProvider> ItemProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(KayaRegistries.ITEM_PROVIDER_TYPE, id, new ItemProvider.Type<>(mapCodec, streamCodec));
	}

	private static <P extends ItemProvider> ItemProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(Kaya.id(path), mapCodec, streamCodec);
	}

}
