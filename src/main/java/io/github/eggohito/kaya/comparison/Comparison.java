package io.github.eggohito.kaya.comparison;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Comparison extends ContextUser {

	Codec<Comparison> CODEC = Type.CODEC.dispatch(Comparison::type, Type::mapCodec);

	StreamCodec<RegistryFriendlyByteBuf, Comparison> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Comparison::type, Type::streamCodec);

	Type<?> type();

	boolean compare(Context context);

	record Type<C extends Comparison>(MapCodec<C> mapCodec, StreamCodec<RegistryFriendlyByteBuf, C> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.COMPARISON_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
