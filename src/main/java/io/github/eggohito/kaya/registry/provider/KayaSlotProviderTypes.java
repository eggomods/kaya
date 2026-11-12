package io.github.eggohito.kaya.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.provider.custom.slot.*;
import io.github.eggohito.kaya.registry.KayaRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class KayaSlotProviderTypes {

	public static final SlotProvider.Type<CompositeConditionalSlotProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalSlotProvider.CODEC, CompositeConditionalSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<ConditionalSlotProvider> CONDITIONAL = registerInternal("conditional", ConditionalSlotProvider.CODEC, ConditionalSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<ContextSlotProvider> CONTEXT = registerInternal("context", ContextSlotProvider.CODEC, ContextSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<WeightedSlotProvider> WEIGHTED = registerInternal("weighted", WeightedSlotProvider.CODEC, WeightedSlotProvider.STREAM_CODEC);

	public static final SlotProvider.Type<FromBlockSlotProvider> FROM_BLOCK = registerInternal("from_block", FromBlockSlotProvider.CODEC, FromBlockSlotProvider.STREAM_CODEC);
	public static final SlotProvider.Type<FromEntitySlotProvider> FROM_ENTITY = registerInternal("from_entity", FromEntitySlotProvider.CODEC, FromEntitySlotProvider.STREAM_CODEC);

	public static void registerAll() {
		SlotProvider.Type.ALIASES.addPathAlias("block", FROM_BLOCK);
		SlotProvider.Type.ALIASES.addPathAlias("entity", FROM_ENTITY);
	}

	public static <P extends SlotProvider> SlotProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(KayaRegistries.SLOT_PROVIDER_TYPE, id, new SlotProvider.Type<>(mapCodec, streamCodec));
	}

	private static <P extends SlotProvider> SlotProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(Kaya.id(path), mapCodec, streamCodec);
	}

}
