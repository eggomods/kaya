package io.github.eggohito.kaya.provider.custom.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.provider.ValueProvider;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.registry.provider.KayaValueProviderKinds;
import io.github.eggohito.kaya.util.CachedBlock;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface BlockProvider extends ValueProvider<CachedBlock> {

	Codec<BlockProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(BlockProvider::getType, Type::mapCodec), ContextBlockProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, BlockProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(BlockProvider::getType, Type::streamCodec);

	@Override
	BlockProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return KayaValueProviderKinds.BLOCK;
	}

	record Type<P extends BlockProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.BLOCK_PROVIDER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
