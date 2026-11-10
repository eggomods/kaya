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

public record CallbackPowerRevokedPower(Action action, Properties properties) implements CallbackPower {

	public static final MapCodec<CallbackPowerRevokedPower> CODEC = CallbackPower.codec(CallbackPowerRevokedPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackPowerRevokedPower> STREAM_CODEC = CallbackPower.streamCodec(CallbackPowerRevokedPower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_POWER_REVOKED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<CallbackPowerRevokedPower> {

		protected Instance(@NotNull CallbackPowerRevokedPower power) {
			super(power);
		}

		@Override
		public void onRevoked(Entity holder) {

			super.onRevoked(holder);
			Context context = createHolderContext(holder);

			if (this.isActive(context)) {
				power.action().execute(context.forChild(".action"));
			}

		}

	}

}
