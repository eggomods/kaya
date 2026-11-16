package io.github.eggohito.kaya.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.condition.custom.ConstantCondition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Condition extends ContextUser {

	Codec<Condition> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(Condition::getType, Type::mapCodec), ConstantCondition.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, Condition> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Condition::getType, Type::streamCodec);

	Type<?> getType();

	boolean test(Context context);

	record Type<C extends Condition>(MapCodec<C> mapCodec, StreamCodec<RegistryFriendlyByteBuf, C> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.CONDITION_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
