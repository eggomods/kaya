package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.CallbackPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record CallbackPowerRemovedPower(Action action, Properties properties) implements CallbackPower {

	public static final MapCodec<CallbackPowerRemovedPower> CODEC = CallbackPower.codec(CallbackPowerRemovedPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackPowerRemovedPower> STREAM_CODEC = CallbackPower.streamCodec(CallbackPowerRemovedPower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_POWER_REMOVED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<CallbackPowerRemovedPower> {

		protected Instance(@NotNull CallbackPowerRemovedPower power) {
			super(power);
		}

		@Override
		public void onRemoved(Entity holder) {

			super.onRemoved(holder);
			Context context = createHolderContext(holder);

			if (this.isActive(context)) {
				power.action().execute(context.forChild(".action"));
			}

		}

	}

}
