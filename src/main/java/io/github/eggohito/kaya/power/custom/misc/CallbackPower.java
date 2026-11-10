package io.github.eggohito.kaya.power.custom.misc;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.action.custom.NothingAction;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.Power;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiFunction;

public interface CallbackPower extends Power {

	Action action();

	@Override
	default void validate(ContextValidator validator) {
		Power.super.validate(validator);
		action().validate(validator.forChild(".action"));
	}

	static <P extends CallbackPower> RecordCodecBuilder<P, Action> optionalActionCodecBuilder() {
		return Action.CODEC.optionalFieldOf("action", NothingAction.INSTANCE).forGetter(CallbackPower::action);
	}

	static <P extends CallbackPower> MapCodec<P> codec(BiFunction<Action, Properties, P> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			Action.CODEC.fieldOf("action").forGetter(CallbackPower::action),
			Power.propertiesCodecBuilder(true)
		).apply(instance, constructor));
	}

	static <P extends CallbackPower> StreamCodec<RegistryFriendlyByteBuf, P> streamCodec(BiFunction<Action, Properties, P> constructor) {
		return StreamCodec.composite(
			Action.STREAM_CODEC, CallbackPower::action,
			Properties.STREAM_CODEC, CallbackPower::properties,
			constructor
		);
	}

}
