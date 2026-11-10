package io.github.eggohito.kaya.registry;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.event.ModifierOrderCallback;
import io.github.eggohito.kaya.modifier.Modifier;
import io.github.eggohito.kaya.modifier.custom.*;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class KayaModifierTypes {

	public static final Modifier.Type<MultiplyModifier> MULTIPLY = registerInternal("multiply", MultiplyModifier.CODEC, MultiplyModifier.STREAM_CODEC);
	public static final Modifier.Type<MultiplyAdditiveModifier> MULTIPLY_ADDITIVE = registerInternal("multiply_additive", MultiplyAdditiveModifier.CODEC, MultiplyAdditiveModifier.STREAM_CODEC);
	public static final Modifier.Type<MultiplyMultiplicativeModifier> MULTIPLY_MULTIPLICATIVE = registerInternal("multiply_multiplicative", MultiplyMultiplicativeModifier.CODEC, MultiplyMultiplicativeModifier.STREAM_CODEC);
	public static final Modifier.Type<DivideModifier> DIVIDE = registerInternal("divide", DivideModifier.CODEC, DivideModifier.STREAM_CODEC);
	public static final Modifier.Type<AddModifier> ADD = registerInternal("add", AddModifier.CODEC, AddModifier.STREAM_CODEC);
	public static final Modifier.Type<MinModifier> MIN = registerInternal("min", MinModifier.CODEC, MinModifier.STREAM_CODEC);
	public static final Modifier.Type<MaxModifier> MAX = registerInternal("max", MaxModifier.CODEC, MaxModifier.STREAM_CODEC);
	public static final Modifier.Type<SetModifier> SET = registerInternal("set", SetModifier.CODEC, SetModifier.STREAM_CODEC);

	public static void registerAll() {

		Modifier.Type.ALIASES.addPathAlias("multiplication", MULTIPLY);
		Modifier.Type.ALIASES.addPathAlias("division", DIVIDE);
		Modifier.Type.ALIASES.addPathAlias("addition", ADD);

		ModifierOrderCallback.EVENT.register(ModifierOrderCallback.PHASE, orderer -> orderer
			.addLast(ADD)
			.addLast(MULTIPLY)
			.addLast(MULTIPLY_ADDITIVE)
			.addLast(MULTIPLY_MULTIPLICATIVE)
			.addLast(DIVIDE)
			.addLast(MIN)
			.addLast(MAX)
			.addLast(SET)
		);

	}

	private static <M extends Modifier> Modifier.Type<M> registerInternal(String path, MapCodec<M> mapCodec, StreamCodec<RegistryFriendlyByteBuf, M> streamCodec) {
		return register(Kaya.id(path), mapCodec, streamCodec);
	}

	public static <M extends Modifier> Modifier.Type<M> register(ResourceLocation id, MapCodec<M> mapCodec, StreamCodec<RegistryFriendlyByteBuf, M> streamCodec) {
		return Registry.register(KayaRegistries.MODIFIER_TYPE, id, new Modifier.Type<>(mapCodec, streamCodec));
	}

}
