package io.github.eggohito.kaya.power.custom.misc;

import com.mojang.datafixers.util.Function7;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.action.custom.NothingAction;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.provider.ValueProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public interface ResourcePower<V, P extends ValueProvider<V>> extends Power {

	P min();

	P max();

	Optional<P> startValue();

	Action onChangedAction();

	Action minAction();

	Action maxAction();

	@Override
	default void validate(ContextValidator validator) {
		Power.super.validate(validator);
		min().validate(validator.forChild(".min"));
		max().validate(validator.forChild(".max"));
		startValue().ifPresent(start -> start.validate(validator.forChild(".start_value")));
		onChangedAction().validate(validator.forChild(".on_changed_action"));
		minAction().validate(validator.forChild(".min_action"));
		maxAction().validate(validator.forChild(".max_action"));
	}

	static <V, P extends ValueProvider<V>, R extends ResourcePower<V, P>> MapCodec<R> mapCodec(Codec<P> providerCodec, Function7<P, P, Optional<P>, Action, Action, Action, Properties, R> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			providerCodec.fieldOf("min").forGetter(ResourcePower::min),
			providerCodec.fieldOf("max").forGetter(ResourcePower::max),
			providerCodec.optionalFieldOf("start_value").forGetter(ResourcePower::startValue),
			Action.CODEC.optionalFieldOf("on_changed_action", NothingAction.INSTANCE).forGetter(ResourcePower::onChangedAction),
			Action.CODEC.optionalFieldOf("min_action", NothingAction.INSTANCE).forGetter(ResourcePower::minAction),
			Action.CODEC.optionalFieldOf("max_action", NothingAction.INSTANCE).forGetter(ResourcePower::maxAction),
			Power.propertiesCodecBuilder(false)
		).apply(instance, constructor));
	}

	static <V, P extends ValueProvider<V>, R extends ResourcePower<V, P>> StreamCodec<RegistryFriendlyByteBuf, R> streamCodec(StreamCodec<RegistryFriendlyByteBuf, P> providerCodec, Function7<P, P, Optional<P>, Action, Action, Action, Properties, R> constructor) {
		return StreamCodec.composite(
			providerCodec, ResourcePower::min,
			providerCodec, ResourcePower::max,
			ByteBufCodecs.optional(providerCodec), ResourcePower::startValue,
			Action.STREAM_CODEC, ResourcePower::onChangedAction,
			Action.STREAM_CODEC, ResourcePower::minAction,
			Action.STREAM_CODEC, ResourcePower::maxAction,
			Properties.STREAM_CODEC, ResourcePower::properties,
			constructor
		);
	}

}
