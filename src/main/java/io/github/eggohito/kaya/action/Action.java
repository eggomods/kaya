package io.github.eggohito.kaya.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.custom.SequenceAction;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Action extends ContextUser {

	MapCodec<Action> MAP_CODEC = Type.CODEC.dispatchMap(Action::getType, Type::mapCodec);

	Codec<Action> CODEC = Codec.recursive(Action.class.getSimpleName(), codec -> new MultiAlternativeCodec<>(MAP_CODEC.codec(), codec.listOf().xmap(SequenceAction::new, SequenceAction::actions)));

	StreamCodec<RegistryFriendlyByteBuf, Action> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Action::getType, Type::streamCodec);

	Type<?> getType();

	void execute(Context context);

	record Type<A extends Action>(MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.ACTION_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
