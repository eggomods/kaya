package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliValueProviderKinds;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface DirectionProvider extends ValueProvider<Direction> {

	Codec<DirectionProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(DirectionProvider::getType, Type::mapCodec), ConstantDirectionProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, DirectionProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(DirectionProvider::getType, Type::streamCodec);

	@Override
	DirectionProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return NeoApoliValueProviderKinds.DIRECTION;
	}

	record Type<P extends DirectionProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.DIRECTION_PROVIDER_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.DIRECTION_PROVIDER_TYPE);

	}

}
