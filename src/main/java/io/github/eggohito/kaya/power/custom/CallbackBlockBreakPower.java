package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CallbackBlockBreakPower(Action onBreakAction, BooleanProvider onlyWhenHarvested, int priority, Properties properties) implements PrioritizedPower<CallbackBlockBreakPower> {

	public static final MapCodec<CallbackBlockBreakPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Action.CODEC.fieldOf("on_break_action").forGetter(CallbackBlockBreakPower::onBreakAction),
		BooleanProvider.CODEC.optionalFieldOf("only_when_harvested", ConstantBooleanProvider.FALSE).forGetter(CallbackBlockBreakPower::onlyWhenHarvested),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(CallbackBlockBreakPower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, CallbackBlockBreakPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackBlockBreakPower> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC, CallbackBlockBreakPower::onBreakAction,
		BooleanProvider.STREAM_CODEC, CallbackBlockBreakPower::onlyWhenHarvested,
		ByteBufCodecs.INT, CallbackBlockBreakPower::priority,
		Properties.STREAM_CODEC, CallbackBlockBreakPower::properties,
		CallbackBlockBreakPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_BLOCK_BREAK;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		PrioritizedPower.super.validate(validator);

		onBreakAction().validate(validator.forChild(".on_break_action"));
		onlyWhenHarvested().validate(validator.forChild(".only_when_harvested"));

	}

	public static class Instance extends Power.Instance<CallbackBlockBreakPower> {

		protected Instance(@NotNull CallbackBlockBreakPower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
			return this.createHolderContextBuilder(holder)
				.withRequired(KayaContextParameters.BROKEN_BLOCK, new CachedBlock(blockPos, blockState, blockEntity))
				.withNullable(KayaContextParameters.BROKEN_SIDE, side)
				.build(holder.level());
		}

		public boolean doesApply(Context context, boolean harvested) {
			return this.isActive(context)
				&& (!power.onlyWhenHarvested().getBoolean(context.forChild(".only_when_harvested")) || harvested);
		}

		public void execute(Context context) {
			power.onBreakAction().execute(context.forChild(".on_break_action"));
		}

	}

	public static void execute(Player breaker, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable Direction side, boolean harvested) {

		for (var instance : new InstanceCollection<>(breaker, Instance.class)) {

			Context context = instance.createContext(breaker, blockPos, blockState, blockEntity, side);

			if (instance.doesApply(context, harvested)) {
				instance.execute(context);
			}

		}

	}

}
