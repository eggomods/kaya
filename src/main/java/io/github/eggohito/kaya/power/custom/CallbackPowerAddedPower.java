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

public record CallbackPowerAddedPower(Action action, Properties properties) implements CallbackPower {

	public static final MapCodec<CallbackPowerAddedPower> CODEC = CallbackPower.codec(CallbackPowerAddedPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackPowerAddedPower> STREAM_CODEC = CallbackPower.streamCodec(CallbackPowerAddedPower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_POWER_ADDED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<CallbackPowerAddedPower> {

		protected Instance(@NotNull CallbackPowerAddedPower power) {
			super(power);
		}

		@Override
		public void onAdded(Entity holder) {

			super.onAdded(holder);
			Context context = createHolderContext(holder);

			if (this.isActive(context)) {
				power.action().execute(context.forChild(".action"));
			}

		}

	}

}
