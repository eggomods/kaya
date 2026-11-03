package io.github.eggohito.kaya.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.provider.custom.vec3.*;
import io.github.eggohito.kaya.registry.KayaRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class KayaVec3ProviderTypes {

	public static final Vec3Provider.Type<CompositeConditionalVec3Provider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalVec3Provider.CODEC, CompositeConditionalVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ConditionalVec3Provider> CONDITIONAL = registerInternal("conditional", ConditionalVec3Provider.CODEC, ConditionalVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ConstantVec3Provider> CONSTANT = registerInternal("constant", ConstantVec3Provider.CODEC, ConstantVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ContextVec3Provider> CONTEXT = registerInternal("context", ContextVec3Provider.CODEC, ContextVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<CrossProductVec3Provider> CROSS_PRODUCT = registerInternal("cross_product", CrossProductVec3Provider.CODEC, CrossProductVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<DynamicVec3Provider> DYNAMIC = registerInternal("dynamic", DynamicVec3Provider.CODEC, DynamicVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<NormalizeVec3Provider> NORMALIZE = registerInternal("normalize", NormalizeVec3Provider.CODEC, NormalizeVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<OffsetVec3Provider> OFFSET = registerInternal("offset", OffsetVec3Provider.CODEC, OffsetVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ProductVec3Provider> PRODUCT = registerInternal("product", ProductVec3Provider.CODEC, ProductVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ReverseVec3Provider> REVERSE = registerInternal("reverse", ReverseVec3Provider.CODEC, ReverseVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<WeightedVec3Provider> WEIGHTED = registerInternal("weighted", WeightedVec3Provider.CODEC, WeightedVec3Provider.STREAM_CODEC);

	public static final Vec3Provider.Type<BlockPositionVec3Provider> BLOCK_POSITION = registerInternal("block/position", BlockPositionVec3Provider.CODEC, BlockPositionVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<DirectionVec3Provider> DIRECTION = registerInternal("direction", DirectionVec3Provider.CODEC, DirectionVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<EntityPositionVec3Provider> ENTITY_POSITION = registerInternal("entity/position", EntityPositionVec3Provider.CODEC, EntityPositionVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<EntityVelocityVec3Provider> ENTITY_VELOCITY = registerInternal("entity/velocity", EntityVelocityVec3Provider.CODEC, EntityVelocityVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<EntityViewVec3Provider> ENTITY_VIEW = registerInternal("entity/view", EntityViewVec3Provider.CODEC, EntityViewVec3Provider.STREAM_CODEC);

	public static void registerAll() {

	}

	private static <P extends Vec3Provider> Vec3Provider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(Kaya.id(path), mapCodec, streamCodec);
	}

	public static <P extends Vec3Provider> Vec3Provider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(KayaRegistries.VEC3_PROVIDER_TYPE, id, new Vec3Provider.Type<>(mapCodec, streamCodec));
	}

}
