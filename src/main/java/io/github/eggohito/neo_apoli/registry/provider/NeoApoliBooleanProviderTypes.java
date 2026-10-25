package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.bool.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliBooleanProviderTypes {

	public static final BooleanProvider.Type<CompositeConditionalBooleanProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalBooleanProvider.CODEC, CompositeConditionalBooleanProvider.STREAM_CODEC);
	public static final BooleanProvider.Type<ConditionalBooleanProvider> CONDITIONAL = registerInternal("conditional", ConditionalBooleanProvider.CODEC, ConditionalBooleanProvider.STREAM_CODEC);
	public static final BooleanProvider.Type<ConstantBooleanProvider> CONSTANT = registerInternal("constant", ConstantBooleanProvider.CODEC, ConstantBooleanProvider.STREAM_CODEC);
	public static final BooleanProvider.Type<ContextBooleanProvider> CONTEXT = registerInternal("context", ContextBooleanProvider.CODEC, ContextBooleanProvider.STREAM_CODEC);
	public static final BooleanProvider.Type<WeightedBooleanProvider> WEIGHTED = registerInternal("weighted", WeightedBooleanProvider.CODEC, WeightedBooleanProvider.STREAM_CODEC);

	public static final BooleanProvider.Type<ConditionResultBooleanProvider> CONDITION_RESULT = registerInternal("condition_result", ConditionResultBooleanProvider.CODEC, ConditionResultBooleanProvider.STREAM_CODEC);
	public static final BooleanProvider.Type<NbtBooleanProvider> NBT = registerInternal("nbt", NbtBooleanProvider.CODEC, NbtBooleanProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	private static <P extends BooleanProvider> BooleanProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends BooleanProvider> BooleanProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.BOOLEAN_PROVIDER_TYPE, id, new BooleanProvider.Type<>(mapCodec, streamCodec));
	}

}
