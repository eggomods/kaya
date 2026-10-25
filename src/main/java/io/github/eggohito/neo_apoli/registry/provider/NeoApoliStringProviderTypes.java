package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.string.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliStringProviderTypes {

	public static final StringProvider.Type<CompositeConditionalStringProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalStringProvider.CODEC, CompositeConditionalStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<ConditionalStringProvider> CONDITIONAL = registerInternal("conditional", ConditionalStringProvider.CODEC, ConditionalStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<ConstantStringProvider> CONSTANT = registerInternal("constant", ConstantStringProvider.CODEC, ConstantStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<ContextStringProvider> CONTEXT = registerInternal("context", ContextStringProvider.CODEC, ContextStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<EscapeStringProvider> ESCAPE = registerInternal("escape", EscapeStringProvider.CODEC, EscapeStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<JoinStringProvider> JOIN = registerInternal("join", JoinStringProvider.CODEC, JoinStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<WeightedStringProvider> WEIGHTED = registerInternal("weighted", WeightedStringProvider.CODEC, WeightedStringProvider.STREAM_CODEC);

	public static final StringProvider.Type<EntityUuidStringProvider> ENTITY_UUID = registerInternal("entity/uuid", EntityUuidStringProvider.CODEC, EntityUuidStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<FromFloatStringProvider> FROM_FLOAT = registerInternal("from_float", FromFloatStringProvider.CODEC, FromFloatStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<FromIntStringProvider> FROM_INT = registerInternal("from_int", FromIntStringProvider.CODEC, FromIntStringProvider.STREAM_CODEC);
	public static final StringProvider.Type<NbtStringProvider> NBT = registerInternal("nbt", NbtStringProvider.CODEC, NbtStringProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	private static <P extends StringProvider> StringProvider.Type<P> registerInternal(java.lang.String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends StringProvider> StringProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.STRING_PROVIDER_TYPE, id, new StringProvider.Type<>(mapCodec, streamCodec));
	}

}
