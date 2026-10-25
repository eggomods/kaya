package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.entity.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliEntityProviderTypes {

	public static final EntityProvider.Type<CompositeConditionalEntityProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalEntityProvider.CODEC, CompositeConditionalEntityProvider.STREAM_CODEC);
	public static final EntityProvider.Type<ConditionalEntityProvider> CONDITIONAL = registerInternal("conditional", ConditionalEntityProvider.CODEC, ConditionalEntityProvider.STREAM_CODEC);
	public static final EntityProvider.Type<ContextEntityProvider> CONTEXT = registerInternal("context", ContextEntityProvider.CODEC, ContextEntityProvider.STREAM_CODEC);
	public static final EntityProvider.Type<WeightedEntityProvider> WEIGHTED = registerInternal("weighted", WeightedEntityProvider.CODEC, WeightedEntityProvider.STREAM_CODEC);

	public static final EntityProvider.Type<SelectorEntityProvider> SELECTOR = registerInternal("selector", SelectorEntityProvider.CODEC, SelectorEntityProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <P extends EntityProvider> EntityProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.ENTITY_PROVIDER_TYPE, id, new EntityProvider.Type<>(mapCodec, streamCodec));
	}

	private static <P extends EntityProvider> EntityProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

}
