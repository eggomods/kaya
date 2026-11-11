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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ModifyBlockSelectablePower(BooleanProvider allow, int priority, Properties properties) implements PrioritizedPower<ModifyBlockSelectablePower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyBlockSelectablePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BooleanProvider.CODEC.fieldOf("allow").forGetter(ModifyBlockSelectablePower::allow),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyBlockSelectablePower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyBlockSelectablePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyBlockSelectablePower> STREAM_CODEC = StreamCodec.composite(
		BooleanProvider.STREAM_CODEC, ModifyBlockSelectablePower::allow,
		ByteBufCodecs.INT, ModifyBlockSelectablePower::priority,
		Properties.STREAM_CODEC, ModifyBlockSelectablePower::properties,
		ModifyBlockSelectablePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_BLOCK_SELECTABLE;
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

	public static class Instance extends Power.Instance<ModifyBlockSelectablePower> {

		protected Instance(@NotNull ModifyBlockSelectablePower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
			return this.createHolderContextBuilder(holder)
				.withRequired(KayaContextParameters.SELECTED_BLOCK, new CachedBlock(blockPos, blockState, blockEntity))
				.build(holder.level());
		}

		public boolean isAllowed(Context context) {
			return power.allow().getBoolean(context.forChild(".allow"));
		}

	}

	public static boolean shouldBeEmpty(Entity entity, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {

		for (var instance : new InstanceCollection<>(entity, Instance.class)) {

			Context context = instance.createContext(entity, blockPos, blockState, blockEntity);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					return !instance.isAllowed(context);
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

}
