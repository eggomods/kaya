package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.NothingAction;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record IntResourcePower(IntProvider min, IntProvider max, Optional<IntProvider> startValue, Action onChangedAction, Action minAction, Action maxAction) implements Power {

	public static final MapCodec<IntResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.fieldOf("min").forGetter(IntResourcePower::min),
		IntProvider.CODEC.fieldOf("max").forGetter(IntResourcePower::max),
		IntProvider.CODEC.optionalFieldOf("start_value").forGetter(IntResourcePower::startValue),
		Action.CODEC.optionalFieldOf("on_changed_action", NothingAction.INSTANCE).forGetter(IntResourcePower::onChangedAction),
		Action.CODEC.optionalFieldOf("min_action", NothingAction.INSTANCE).forGetter(IntResourcePower::minAction),
		Action.CODEC.optionalFieldOf("max_action", NothingAction.INSTANCE).forGetter(IntResourcePower::maxAction)
	).apply(instance, IntResourcePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IntResourcePower> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, IntResourcePower::min,
		IntProvider.STREAM_CODEC, IntResourcePower::max,
		ByteBufCodecs.optional(IntProvider.STREAM_CODEC), IntResourcePower::startValue,
		Action.STREAM_CODEC, IntResourcePower::onChangedAction,
		Action.STREAM_CODEC, IntResourcePower::minAction,
		Action.STREAM_CODEC, IntResourcePower::maxAction,
		IntResourcePower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.RESOURCE_INT;
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
	public static final class Instance extends Power.Instance<IntResourcePower> {

		private static final MapCodec<Integer> VALUE_CODEC = Codec.INT.fieldOf("value");
		private int value;

		Instance(@NotNull IntResourcePower power) {
			super(power);
		}

		@Override
		public void onGranted(Entity holder) {

			Context context = this.createHolderContext(holder);
			int startValue = power.startValue()
				.map(start -> start.getInt(context.forChild(".start_value")))
				.orElseGet(() -> power.min().getInt(context.forChild(".min")));

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

		public boolean setValue(Entity holder, int value) {

			Context context = this.createHolderContext(holder);
			int prevValue = this.getValue();

			int min = power.min().getInt(context.forChild(".min"));
			int max = power.max().getInt(context.forChild(".max"));

			int nextValue = Mth.clamp(value, min, max);
			this.value = nextValue;

			if (nextValue != prevValue) {

				power.onChangedAction().execute(context.forChild(".on_changed_action"));

				if (nextValue >= max) {
					power.maxAction().execute(context.forChild(".max_action"));
				}

				else if (nextValue <= min) {
					power.minAction().execute(context.forChild(".min_action"));
				}

				return true;

			}

			return false;

		}

		public void setAndSyncValue(Entity holder, int value) {

			if (this.setValue(holder, value)) {
				this.syncData(holder);
			}

		}

	}

}
