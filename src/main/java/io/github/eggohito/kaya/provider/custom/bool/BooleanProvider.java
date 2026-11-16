package io.github.eggohito.kaya.provider.custom.bool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.provider.ValueProvider;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.registry.provider.KayaValueProviderKinds;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface BooleanProvider extends ValueProvider<Boolean> {

	Codec<BooleanProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(BooleanProvider::getType, Type::mapCodec), ContextBooleanProvider.INLINE_CODEC, ConstantBooleanProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, BooleanProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(BooleanProvider::getType, Type::streamCodec);

	@NotNull
	BooleanProvider.Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return KayaValueProviderKinds.BOOLEAN;
	}

	default boolean getBoolean(Context context) {
		return this.getValue(context).orElse(false);
	}

	record Type<P extends BooleanProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.BOOLEAN_PROVIDER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
