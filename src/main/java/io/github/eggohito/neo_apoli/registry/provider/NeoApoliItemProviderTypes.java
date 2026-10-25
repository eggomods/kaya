package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.item.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliItemProviderTypes {

	public static final ItemProvider.Type<CompositeConditionalItemProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalItemProvider.CODEC, CompositeConditionalItemProvider.STREAM_CODEC);
	public static final ItemProvider.Type<ConditionalItemProvider> CONDITIONAL = registerInternal("conditional", ConditionalItemProvider.CODEC, ConditionalItemProvider.STREAM_CODEC);
	public static final ItemProvider.Type<ContextItemProvider> CONTEXT = registerInternal("context", ContextItemProvider.CODEC, ContextItemProvider.STREAM_CODEC);
	public static final ItemProvider.Type<WeightedItemProvider> WEIGHTED = registerInternal("weighted", WeightedItemProvider.CODEC, WeightedItemProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <P extends ItemProvider> ItemProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.ITEM_PROVIDER_TYPE, id, new ItemProvider.Type<>(mapCodec, streamCodec));
	}

	private static <P extends ItemProvider> ItemProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

}
