package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.parameter.BlockContextParameter;
import io.github.eggohito.kaya.exception.PosOutOfBoundsException;
import io.github.eggohito.kaya.exception.PosUnloadedException;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.CallbackPower;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record CallbackPlayerWakeUpPower(Action action, Properties properties) implements CallbackPower {

	public static final BlockContextParameter SLEPT_ON_BLOCK = new BlockContextParameter(Kaya.id("slept_on_block"));

	public static final MapCodec<CallbackPlayerWakeUpPower> CODEC = CallbackPower.codec(CallbackPlayerWakeUpPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackPlayerWakeUpPower> STREAM_CODEC = CallbackPower.streamCodec(CallbackPlayerWakeUpPower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_PLAYER_WAKE_UP;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<CallbackPlayerWakeUpPower> {

		protected Instance(@NotNull CallbackPlayerWakeUpPower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockPos sleepingPos) {

			Level level = holder.level();
			CachedBlock sleptOnBlock = CachedBlock.fromLoadedPos(level, sleepingPos);

			return this.createHolderContextBuilder(holder)
				.withRequired(SLEPT_ON_BLOCK, sleptOnBlock)
				.build(level);

		}

		public void execute(Context context) {
			power.action().execute(context.forChild(".action"));
		}

	}

	public static void execute(Player sleeper, BlockPos sleepingPos) {

		for (var instance : Powers.getInstances(sleeper, Instance.class)) {

			try {

				Context context = instance.createContext(sleeper, sleepingPos);

				if (instance.isActive(context)) {
					instance.execute(context);
				}

			}

			catch (PosUnloadedException | PosOutOfBoundsException ignored) {
				//  No-op; just need to soft error
			}

		}

	}

}
