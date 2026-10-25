package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.effect.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliEffectProviderTypes {

	public static final EffectProvider.Type<CompositeConditionalEffectProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalEffectProvider.CODEC, CompositeConditionalEffectProvider.STREAM_CODEC);
	public static final EffectProvider.Type<ConditionalEffectProvider> CONDITIONAL = registerInternal("conditional", ConditionalEffectProvider.CODEC, ConditionalEffectProvider.STREAM_CODEC);
	public static final EffectProvider.Type<ConstantEffectProvider> CONSTANT = registerInternal("constant", ConstantEffectProvider.CODEC, ConstantEffectProvider.STREAM_CODEC);
	public static final EffectProvider.Type<ContextEffectProvider> CONTEXT = registerInternal("context", ContextEffectProvider.CODEC, ContextEffectProvider.STREAM_CODEC);
	public static final EffectProvider.Type<WeightedEffectProvider> WEIGHTED = registerInternal("weighted", WeightedEffectProvider.CODEC, WeightedEffectProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <P extends EffectProvider> EffectProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.EFFECT_PROVIDER_TYPE, id, new EffectProvider.Type<>(mapCodec, streamCodec));
	}

	private static <P extends EffectProvider> EffectProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

}
