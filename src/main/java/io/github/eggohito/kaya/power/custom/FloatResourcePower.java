package io.github.eggohito.kaya.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.ResourcePower;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FloatResourcePower(FloatProvider min, FloatProvider max, Optional<FloatProvider> startValue, Action onChangedAction, Action minAction, Action maxAction, Properties properties) implements ResourcePower<Float, FloatProvider> {

	public static final MapCodec<FloatResourcePower> CODEC = ResourcePower.mapCodec(FloatProvider.CODEC, FloatResourcePower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, FloatResourcePower> STREAM_CODEC = ResourcePower.streamCodec(FloatProvider.STREAM_CODEC, FloatResourcePower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.RESOURCE_FLOAT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
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

			this.setAndSyncValueNoCallbacks(holder, startValue);

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

		public boolean setValue(Entity holder, float value, boolean executeActions) {

			if (holder.level().isClientSide()) {
				return false;
			}

			Context context = this.createHolderContext(holder);
			float prevValue = this.getValue();

			float min = power.min().getFloat(context.forChild(".min"));
			float max = power.max().getFloat(context.forChild(".max"));

			float nextValue = Mth.clamp(value, min, max);
			this.value = nextValue;

			if (Float.compare(nextValue, prevValue) != 0) {

				if (executeActions) {

					power.onChangedAction().execute(context.forChild(".on_changed_action"));

					if (Float.compare(nextValue, max) >= 1) {
						power.maxAction().execute(context.forChild(".max_action"));
					}

					else if (Float.compare(nextValue, min) <= 0) {
						power.minAction().execute(context.forChild(".min_action"));
					}

				}

				return true;

			}

			return false;

		}

		public boolean setValue(Entity holder, float value) {
			return setValue(holder, value, true);
		}

		public boolean setValueNoCallbacks(Entity holder, float value) {
			return setValue(holder, value, false);
		}

		public void setAndSyncValue(Entity holder, float value) {

			if (this.setValue(holder, value)) {
				this.syncData(holder);
			}

		}

		public void setAndSyncValueNoCallbacks(Entity holder, float value) {

			if (this.setValueNoCallbacks(holder, value)) {
				this.syncData(holder);
			}

		}

	}

}
