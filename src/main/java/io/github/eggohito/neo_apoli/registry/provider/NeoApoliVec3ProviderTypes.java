package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.vec3.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliVec3ProviderTypes {

	public static final Vec3Provider.Type<CompositeConditionalVec3Provider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalVec3Provider.CODEC, CompositeConditionalVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ConditionalVec3Provider> CONDITIONAL = registerInternal("conditional", ConditionalVec3Provider.CODEC, ConditionalVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ConstantVec3Provider> CONSTANT = registerInternal("constant", ConstantVec3Provider.CODEC, ConstantVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<ContextVec3Provider> CONTEXT = registerInternal("context", ContextVec3Provider.CODEC, ContextVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<DynamicVec3Provider> DYNAMIC = registerInternal("dynamic", DynamicVec3Provider.CODEC, DynamicVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<OffsetVec3Provider> OFFSET = registerInternal("offset", OffsetVec3Provider.CODEC, OffsetVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<WeightedVec3Provider> WEIGHTED = registerInternal("weighted", WeightedVec3Provider.CODEC, WeightedVec3Provider.STREAM_CODEC);

	public static final Vec3Provider.Type<BlockPositionVec3Provider> BLOCK_POSITION = registerInternal("block/position", BlockPositionVec3Provider.CODEC, BlockPositionVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<DirectionVec3Provider> DIRECTION = registerInternal("direction", DirectionVec3Provider.CODEC, DirectionVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<EntityPositionVec3Provider> ENTITY_POSITION = registerInternal("entity/position", EntityPositionVec3Provider.CODEC, EntityPositionVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<EntityVelocityVec3Provider> ENTITY_VELOCITY = registerInternal("entity/velocity", EntityVelocityVec3Provider.CODEC, EntityVelocityVec3Provider.STREAM_CODEC);
	public static final Vec3Provider.Type<EntityViewVec3Provider> ENTITY_VIEW = registerInternal("entity/view", EntityViewVec3Provider.CODEC, EntityViewVec3Provider.STREAM_CODEC);

	public static void registerAll() {

	}

	private static <P extends Vec3Provider> Vec3Provider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends Vec3Provider> Vec3Provider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.VEC3_PROVIDER_TYPE, id, new Vec3Provider.Type<>(mapCodec, streamCodec));
	}

}
