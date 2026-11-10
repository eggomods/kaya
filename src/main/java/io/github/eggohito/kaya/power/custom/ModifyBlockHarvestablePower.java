package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public record ModifyBlockHarvestablePower(BooleanProvider allow, int priority, Properties properties) implements PrioritizedPower<ModifyBlockHarvestablePower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyBlockHarvestablePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BooleanProvider.CODEC.fieldOf("allow").forGetter(ModifyBlockHarvestablePower::allow),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyBlockHarvestablePower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyBlockHarvestablePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyBlockHarvestablePower> STREAM_CODEC = StreamCodec.composite(
		BooleanProvider.STREAM_CODEC, ModifyBlockHarvestablePower::allow,
		ByteBufCodecs.INT, ModifyBlockHarvestablePower::priority,
		Properties.STREAM_CODEC, ModifyBlockHarvestablePower::properties,
		ModifyBlockHarvestablePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_BLOCK_HARVESTABLE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		allow().validate(validator.forChild(".allow"));
	}

	public static class Instance extends Power.Instance<ModifyBlockHarvestablePower> {

		protected Instance(@NotNull ModifyBlockHarvestablePower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
			return this.createHolderContextBuilder(holder)
				.withRequired(KayaContextParameters.MINING_BLOCK, new CachedBlock(blockPos, blockState, blockEntity))
				.build(holder.level());
		}

		public boolean isAllowed(Context context) {
			return power.allow().getBoolean(context.forChild(".allow"));
		}

	}

	public static boolean modify(Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, BooleanSupplier defaultValue) {

		for (var instance : new InstanceCollection<>(player, Instance.class)) {

			Context context = instance.createContext(player, blockPos, blockState, blockEntity);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					return instance.isAllowed(context);
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return defaultValue.getAsBoolean();

	}

}
