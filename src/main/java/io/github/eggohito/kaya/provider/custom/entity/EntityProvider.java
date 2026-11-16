package io.github.eggohito.kaya.provider.custom.entity;

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
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface EntityProvider extends ValueProvider<Entity> {

	Codec<EntityProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(EntityProvider::getType, Type::mapCodec), ContextEntityProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, EntityProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(EntityProvider::getType, Type::streamCodec);

	@Override
	EntityProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return KayaValueProviderKinds.ENTITY;
	}

	record Type<P extends EntityProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.ENTITY_PROVIDER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
