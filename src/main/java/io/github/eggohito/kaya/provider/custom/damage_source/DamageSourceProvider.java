package io.github.eggohito.kaya.provider.custom.damage_source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.provider.ValueProvider;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.registry.provider.KayaValueProviderKinds;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

public interface DamageSourceProvider extends ValueProvider<DamageSource> {

	Codec<DamageSourceProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(DamageSourceProvider::getType, Type::mapCodec), ContextDamageSourceProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, DamageSourceProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(DamageSourceProvider::getType, Type::streamCodec);

	@Override
	DamageSourceProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return KayaValueProviderKinds.DAMAGE_SOURCE;
	}

	record Type<P extends DamageSourceProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.DAMAGE_SOURCE_PROVIDER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
