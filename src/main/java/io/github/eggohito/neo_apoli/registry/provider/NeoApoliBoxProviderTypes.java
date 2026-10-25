package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.box.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliBoxProviderTypes {

	public static final BoxProvider.Type<CompositeConditionalBoxProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalBoxProvider.CODEC, CompositeConditionalBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<ConditionalBoxProvider> CONDITIONAL = registerInternal("conditional", ConditionalBoxProvider.CODEC, ConditionalBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<ConstantBoxProvider> CONSTANT = registerInternal("constant", ConstantBoxProvider.CODEC, ConstantBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<ContextBoxProvider> CONTEXT = registerInternal("context", ContextBoxProvider.CODEC, ContextBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<DynamicBoxProvider> DYNAMIC = registerInternal("dynamic", DynamicBoxProvider.CODEC, DynamicBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<InflateBoxProvider> INFLATE = registerInternal("inflate", InflateBoxProvider.CODEC, InflateBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<ResizeBoxProvider> RESIZE = registerInternal("resize", ResizeBoxProvider.CODEC, ResizeBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<TranslateBoxProvider> TRANSLATE = registerInternal("translate", TranslateBoxProvider.CODEC, TranslateBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<WeightedBoxProvider> WEIGHTED = registerInternal("weighted", WeightedBoxProvider.CODEC, WeightedBoxProvider.STREAM_CODEC);

	public static final BoxProvider.Type<BlockBoundsBoxProvider> BLOCK_BOUNDS = registerInternal("block/bounds", BlockBoundsBoxProvider.CODEC, BlockBoundsBoxProvider.STREAM_CODEC);
	public static final BoxProvider.Type<EntityBoundsBoxProvider> ENTITY_BOUNDS = registerInternal("entity/bounds", EntityBoundsBoxProvider.CODEC, EntityBoundsBoxProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	private static <P extends BoxProvider> BoxProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends BoxProvider> BoxProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.BOX_PROVIDER_TYPE, id, new BoxProvider.Type<>(mapCodec, streamCodec));
	}

}
