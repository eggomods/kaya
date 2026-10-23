package io.github.eggohito.neo_apoli.provider.custom.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliValueProviderKinds;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface BlockProvider extends ValueProvider<CachedBlock> {

	Codec<BlockProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(BlockProvider::getType, Type::mapCodec), ContextBlockProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, BlockProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(BlockProvider::getType, Type::streamCodec);

	@Override
	BlockProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return NeoApoliValueProviderKinds.BLOCK;
	}

	record Type<P extends BlockProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.BLOCK_PROVIDER_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.BLOCK_PROVIDER_TYPE);

	}

}
