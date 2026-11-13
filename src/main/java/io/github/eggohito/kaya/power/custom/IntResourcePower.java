package io.github.eggohito.kaya.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.ResourcePower;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record IntResourcePower(IntProvider min, IntProvider max, Optional<IntProvider> startValue, Action onChangedAction, Action minAction, Action maxAction, Properties properties) implements ResourcePower<Integer, IntProvider> {

	public static final MapCodec<IntResourcePower> CODEC = ResourcePower.mapCodec(IntProvider.CODEC, IntResourcePower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, IntResourcePower> STREAM_CODEC = ResourcePower.streamCodec(IntProvider.STREAM_CODEC, IntResourcePower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.RESOURCE_INT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
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

		public boolean setValue(Entity holder, int value, boolean executeActions) {

			if (holder.level().isClientSide()) {
				return false;
			}

			Context context = this.createHolderContext(holder);
			int prevValue = this.getValue();

			int min = power.min().getInt(context.forChild(".min"));
			int max = power.max().getInt(context.forChild(".max"));

			int nextValue = Mth.clamp(value, min, max);
			this.value = nextValue;

			if (nextValue != prevValue) {

				if (executeActions) {

					power.onChangedAction().execute(context.forChild(".on_changed_action"));

					if (nextValue >= max) {
						power.maxAction().execute(context.forChild(".max_action"));
					}

					else if (nextValue <= min) {
						power.minAction().execute(context.forChild(".min_action"));
					}

				}

				return true;

			}

			return false;

		}

		public boolean setValue(Entity holder, int value) {
			return setValue(holder, value, true);
		}

		public boolean setValueNoCallbacks(Entity holder, int value) {
			return setValue(holder, value, false);
		}

		public void setAndSyncValue(Entity holder, int value) {

			if (this.setValue(holder, value)) {
				this.syncData(holder);
			}

		}

		public void setAndSyncValueNoCallbacks(Entity holder, int value) {

			if (this.setValueNoCallbacks(holder, value)) {
				this.syncData(holder);
			}

		}

	}

}
