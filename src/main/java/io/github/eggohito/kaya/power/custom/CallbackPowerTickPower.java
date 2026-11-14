package io.github.eggohito.kaya.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.action.custom.NothingAction;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.CallbackPower;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record CallbackPowerTickPower(Action action, Action risingAction, Action fallingAction, IntProvider interval, Properties properties) implements CallbackPower {

	public static final MapCodec<CallbackPowerTickPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		CallbackPower.optionalActionCodecBuilder(),
		Action.CODEC.optionalFieldOf("rising_action", NothingAction.INSTANCE).forGetter(CallbackPowerTickPower::risingAction),
		Action.CODEC.optionalFieldOf("falling_action", NothingAction.INSTANCE).forGetter(CallbackPowerTickPower::fallingAction),
		IntProvider.clamped(1, Integer.MAX_VALUE).optionalFieldOf("interval", new ConstantIntProvider(20)).forGetter(CallbackPowerTickPower::interval),
		Power.propertiesCodecBuilder(true)
	).apply(instance, CallbackPowerTickPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackPowerTickPower> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC, CallbackPowerTickPower::action,
		Action.STREAM_CODEC, CallbackPowerTickPower::risingAction,
		Action.STREAM_CODEC, CallbackPowerTickPower::fallingAction,
		IntProvider.STREAM_CODEC, CallbackPowerTickPower::interval,
		Properties.STREAM_CODEC, CallbackPowerTickPower::properties,
		CallbackPowerTickPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_POWER_TICK;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		CallbackPower.super.validate(validator);

		risingAction().validate(validator.forChild(".rising_action"));
		fallingAction().validate(validator.forChild(".falling_action"));
		interval().validate(validator.forChild(".interval"));

	}

	public static class Instance extends Power.Instance<CallbackPowerTickPower> {

		private static final MapCodec<Boolean> WAS_ACTIVE_CODEC = Codec.BOOL.optionalFieldOf("was_active", false);

		private boolean currentlyActive;
		private boolean wasActive;
		private long tick;

		protected Instance(@NotNull CallbackPowerTickPower power) {
			super(power);
		}

		//  TODO: optimize?
		@Override
		public void onTick(Entity holder) {

			Context context = createHolderContext(holder);
			int interval = power.interval().getInt(context.forChild(".interval"));

			if (context.hasProblems()) {
				this.currentlyActive = false;
				this.wasActive = false;
			}

			else if (this.isActive(context)) {

				if (!currentlyActive) {
					this.currentlyActive = true;
					this.tick = 0;
				}

				else if (tick % interval == 0) {

					if (!wasActive) {
						power.risingAction().execute(context.forChild(".rising_action"));
					}

					power.action().execute(context.forChild(".action"));
					this.wasActive = true;

				}

			}

			else if (wasActive) {

				if (currentlyActive) {
					this.currentlyActive = false;
					this.tick = 0;
				}

				else if (tick % interval == 0) {
					power.fallingAction().execute(context.forChild(".falling_action"));
					this.wasActive = false;
				}

			}

			this.tick++;

		}

		@Override
		public boolean shouldTick(Entity holder) {
			return true;
		}

		@Override
		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {
			return WAS_ACTIVE_CODEC.decode(ops, mapInput)
				.ifSuccess(value -> this.wasActive = value)
				.map(ignored -> Unit.INSTANCE);
		}

		@Override
		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {
			return WAS_ACTIVE_CODEC.encode(this.wasActive, ops, prefix);
		}

	}

}
