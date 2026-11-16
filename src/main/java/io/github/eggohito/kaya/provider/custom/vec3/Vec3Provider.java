package io.github.eggohito.kaya.provider.custom.vec3;

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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface Vec3Provider extends ValueProvider<Vec3> {

	Codec<Vec3Provider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(Vec3Provider::getType, Type::mapCodec), ContextVec3Provider.INLINE_CODEC, ConstantVec3Provider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, Vec3Provider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Vec3Provider::getType, Type::streamCodec);

	@NotNull
	Vec3Provider.Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return KayaValueProviderKinds.VEC3;
	}

	record Type<P extends Vec3Provider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.VEC3_PROVIDER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
