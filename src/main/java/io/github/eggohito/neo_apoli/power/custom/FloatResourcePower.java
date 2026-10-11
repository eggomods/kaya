package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.NothingAction;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FloatResourcePower(FloatProvider min, FloatProvider max, Optional<FloatProvider> startValue, Action onChangedAction, Action minAction, Action maxAction) implements Power {

	public static final MapCodec<FloatResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.CODEC.fieldOf("min").forGetter(FloatResourcePower::min),
		FloatProvider.CODEC.fieldOf("max").forGetter(FloatResourcePower::max),
		FloatProvider.CODEC.optionalFieldOf("start_value").forGetter(FloatResourcePower::startValue),
		Action.CODEC.optionalFieldOf("on_changed_action", NothingAction.INSTANCE).forGetter(FloatResourcePower::onChangedAction),
		Action.CODEC.optionalFieldOf("min_action", NothingAction.INSTANCE).forGetter(FloatResourcePower::minAction),
		Action.CODEC.optionalFieldOf("max_action", NothingAction.INSTANCE).forGetter(FloatResourcePower::maxAction)
	).apply(instance, FloatResourcePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FloatResourcePower> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, FloatResourcePower::min,
		FloatProvider.STREAM_CODEC, FloatResourcePower::max,
		ByteBufCodecs.optional(FloatProvider.STREAM_CODEC), FloatResourcePower::startValue,
		Action.STREAM_CODEC, FloatResourcePower::onChangedAction,
		Action.STREAM_CODEC, FloatResourcePower::minAction,
		Action.STREAM_CODEC, FloatResourcePower::maxAction,
		FloatResourcePower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.RESOURCE_FLOAT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		min().validate(validator.forChild(".min"));
		max().validate(validator.forChild(".max"));
		startValue().ifPresent(start -> start.validate(validator.forChild(".start_value")));
		onChangedAction().validate(validator.forChild(".on_changed_action"));
		minAction().validate(validator.forChild(".min_action"));
		maxAction().validate(validator.forChild(".max_action"));
	}

	@Getter
	public static final class Instance extends Power.Instance<FloatResourcePower> {

		private static final MapCodec<Float> VALUE_CODEC = Codec.FLOAT.fieldOf("value");
		private float value;

		Instance(@NotNull FloatResourcePower power) {
			super(power);
		}

		@Override
		public void onGranted(Entity holder) {

			Context context = this.createHolderContext(holder);
			float startValue = power.startValue()
				.map(start -> start.getFloat(context.forChild(".start_value")))
				.orElseGet(() -> power.min().getFloat(context.forChild(".min")));

			this.setAndSyncValue(holder, startValue);

		}

		@Override
		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {
			return VALUE_CODEC.decode(ops, mapInput)
				.map(value -> this.value = value)
				.map(ignored -> Unit.INSTANCE);
		}

		@Override
		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {
			return VALUE_CODEC.encode(this.value, ops, prefix);
		}

		public boolean setValue(Entity holder, float value) {

			Context context = this.createHolderContext(holder);
			float prevValue = this.getValue();

			float min = power.min().getFloat(context.forChild(".min"));
			float max = power.max().getFloat(context.forChild(".max"));

			float nextValue = Mth.clamp(value, min, max);
			this.value = nextValue;

			if (Float.compare(nextValue, prevValue) != 0) {

				power.onChangedAction().execute(context.forChild(".on_changed_action"));

				if (Float.compare(nextValue, max) >= 1) {
					power.maxAction().execute(context.forChild(".max_action"));
				}

				else if (Float.compare(nextValue, min) <= 0) {
					power.minAction().execute(context.forChild(".min_action"));
				}

				return true;

			}

			return false;

		}

		public void setAndSyncValue(Entity holder, float value) {

			if (this.setValue(holder, value)) {
				this.syncData(holder);
			}

		}

	}

}
