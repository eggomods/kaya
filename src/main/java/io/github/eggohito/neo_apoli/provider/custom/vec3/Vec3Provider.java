package io.github.eggohito.neo_apoli.provider.custom.vec3;

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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface Vec3Provider extends ValueProvider<Vec3> {

	Codec<Vec3Provider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(Vec3Provider::getType, Type::mapCodec), ContextVec3Provider.INLINE_CODEC, ConstantVec3Provider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, Vec3Provider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Vec3Provider::getType, Type::streamCodec);

	@NotNull
	Vec3Provider.Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return NeoApoliValueProviderKinds.VEC3;
	}

	record Type<P extends Vec3Provider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.VEC3_PROVIDER_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.VEC3_PROVIDER_TYPE);

	}

}
