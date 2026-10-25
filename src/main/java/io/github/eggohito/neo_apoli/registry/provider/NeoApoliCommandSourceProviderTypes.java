package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.command_source.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliCommandSourceProviderTypes {

	public static final CommandSourceProvider.Type<CompositeConditionalCommandSourceProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalCommandSourceProvider.CODEC, CompositeConditionalCommandSourceProvider.STREAM_CODEC);
	public static final CommandSourceProvider.Type<ConditionalCommandSourceProvider> CONDITIONAL = registerInternal("conditional", ConditionalCommandSourceProvider.CODEC, ConditionalCommandSourceProvider.STREAM_CODEC);
	public static final CommandSourceProvider.Type<ContextCommandSourceProvider> CONTEXT = registerInternal("context", ContextCommandSourceProvider.CODEC, ContextCommandSourceProvider.STREAM_CODEC);
	public static final CommandSourceProvider.Type<WeightedCommandSourceProvider> WEIGHTED = registerInternal("weighted", WeightedCommandSourceProvider.CODEC, WeightedCommandSourceProvider.STREAM_CODEC);

	public static final CommandSourceProvider.Type<BlockCommandSourceProvider> BLOCK = registerInternal("block", BlockCommandSourceProvider.CODEC, BlockCommandSourceProvider.STREAM_CODEC);
	public static final CommandSourceProvider.Type<EntityCommandSourceProvider> ENTITY = registerInternal("entity", EntityCommandSourceProvider.CODEC, EntityCommandSourceProvider.STREAM_CODEC);
	public static final CommandSourceProvider.Type<ServerCommandSourceProvider> SERVER = registerInternal("server", ServerCommandSourceProvider.INSTANCE);

	public static void registerAll() {

	}

	public static <P extends CommandSourceProvider> CommandSourceProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.COMMAND_SOURCE_PROVIDER_TYPE, id, new CommandSourceProvider.Type<>() {

			@Override
			public MapCodec<P> mapCodec() {
				return mapCodec;
			}

			@Override
			public StreamCodec<RegistryFriendlyByteBuf, P> streamCodec() {
				return streamCodec;
			}

		});
	}

	private static <P extends CommandSourceProvider> CommandSourceProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends CommandSourceProvider, T extends CommandSourceProvider.Type<P>> T register(ResourceLocation id, T type) {
		return Registry.register(NeoApoliRegistries.COMMAND_SOURCE_PROVIDER_TYPE, id, type);
	}

	private static <P extends CommandSourceProvider, T extends CommandSourceProvider.Type<P>> T registerInternal(String path, T type) {
		return register(NeoApoli.id(path), type);
	}

}
