package io.github.eggohito.neo_apoli.provider.custom.damage_source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliValueProviderKinds;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
		return NeoApoliValueProviderKinds.DAMAGE_SOURCE;
	}

	record Type<P extends DamageSourceProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.DAMAGE_SOURCE_PROVIDER_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.DAMAGE_SOURCE_PROVIDER_TYPE);

	}

}
