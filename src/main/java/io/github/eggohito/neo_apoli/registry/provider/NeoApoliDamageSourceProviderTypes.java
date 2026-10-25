package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.damage_source.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliDamageSourceProviderTypes {

	public static final DamageSourceProvider.Type<CompositeConditionalDamageSourceProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalDamageSourceProvider.CODEC, CompositeConditionalDamageSourceProvider.STREAM_CODEC);
	public static final DamageSourceProvider.Type<ConditionalDamageSourceProvider> CONDITIONAL = registerInternal("conditional", ConditionalDamageSourceProvider.CODEC, ConditionalDamageSourceProvider.STREAM_CODEC);
	public static final DamageSourceProvider.Type<ContextDamageSourceProvider> CONTEXT = registerInternal("context", ContextDamageSourceProvider.CODEC, ContextDamageSourceProvider.STREAM_CODEC);
	public static final DamageSourceProvider.Type<WeightedDamageSourceProvider> WEIGHTED = registerInternal("weighted", WeightedDamageSourceProvider.CODEC, WeightedDamageSourceProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <P extends DamageSourceProvider> DamageSourceProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.DAMAGE_SOURCE_PROVIDER_TYPE, id, new DamageSourceProvider.Type<>(mapCodec, streamCodec));
	}

	private static <P extends DamageSourceProvider> DamageSourceProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

}
