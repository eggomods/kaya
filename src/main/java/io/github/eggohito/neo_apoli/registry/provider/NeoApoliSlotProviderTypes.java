package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.slot.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliSlotProviderTypes {

	public static final SlotProvider.Type<CompositeConditionalSlotProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalSlotProvider.CODEC, CompositeConditionalSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<ConditionalSlotProvider> CONDITIONAL = registerInternal("conditional", ConditionalSlotProvider.CODEC, ConditionalSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<ContextSlotProvider> CONTEXT = registerInternal("context", ContextSlotProvider.CODEC, ContextSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<WeightedSlotProvider> WEIGHTED = registerInternal("weighted", WeightedSlotProvider.CODEC, WeightedSlotProvider.STREAM_CODEC);

	public static final SlotProvider.Type<BlockSlotProvider> BLOCK = registerInternal("block", BlockSlotProvider.CODEC, BlockSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<EntitySlotProvider> ENTITY = registerInternal("entity", EntitySlotProvider.CODEC, EntitySlotProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <P extends SlotProvider> SlotProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.SLOT_PROVIDER_TYPE, id, new SlotProvider.Type<>(mapCodec, streamCodec));
	}

	private static <P extends SlotProvider> SlotProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

}
