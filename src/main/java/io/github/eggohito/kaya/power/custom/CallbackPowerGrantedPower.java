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

public record CallbackPowerGrantedPower(Action action, Properties properties) implements CallbackPower {

	public static final MapCodec<CallbackPowerGrantedPower> CODEC = CallbackPower.codec(CallbackPowerGrantedPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackPowerGrantedPower> STREAM_CODEC = CallbackPower.streamCodec(CallbackPowerGrantedPower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_POWER_GRANTED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<CallbackPowerGrantedPower> {

		protected Instance(@NotNull CallbackPowerGrantedPower power) {
			super(power);
		}

		@Override
		public void onGranted(Entity holder) {

			super.onGranted(holder);
			Context context = createHolderContext(holder);

			if (this.isActive(context)) {
				power.action().execute(context.forChild(".action"));
			}

		}

	}

}
