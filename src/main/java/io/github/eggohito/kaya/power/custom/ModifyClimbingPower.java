package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.condition.custom.IsEntitySneakingCondition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.parameter.BlockContextParameter;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.exception.PosOutOfBoundsException;
import io.github.eggohito.kaya.exception.PosUnloadedException;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public record ModifyClimbingPower(Condition holdingCondition, BooleanProvider allowHolding, Properties properties) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();
	public static final BlockContextParameter CLIMBED_BLOCK = new BlockContextParameter(Kaya.id("climbed_block"));

	public static final MapCodec<ModifyClimbingPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.optionalFieldOf("holding_condition", new IsEntitySneakingCondition(KayaContextParameters.THIS_ENTITY)).forGetter(ModifyClimbingPower::holdingCondition),
		BooleanProvider.CODEC.optionalFieldOf("allow_holding", new ConstantBooleanProvider(true)).forGetter(ModifyClimbingPower::allowHolding),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyClimbingPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyClimbingPower> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, ModifyClimbingPower::holdingCondition,
		BooleanProvider.STREAM_CODEC, ModifyClimbingPower::allowHolding,
		Properties.STREAM_CODEC, ModifyClimbingPower::properties,
		ModifyClimbingPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_CLIMBING;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		Power.super.validate(validator);

		holdingCondition().validate(validator.forChild(".holding_condition"));
		allowHolding().validate(validator.forChild(".allow_holding"));

	}

	public static class Instance extends Power.Instance<ModifyClimbingPower> {

		protected Instance(@NotNull ModifyClimbingPower power) {
			super(power);
		}

		@Override
		public Context.Builder createHolderContextBuilder(Entity holder) {

			Level level = holder.level();
			BlockPos blockPos = holder.blockPosition();

			return super.createHolderContextBuilder(holder).withRequired(CLIMBED_BLOCK, CachedBlock.fromLoadedPos(level, blockPos));

		}

		public boolean canHold(Context context) {
			return this.isActive(context)
				&& power.allowHolding().getBoolean(context.forChild(".allow_holding"))
				&& power.holdingCondition().test(context.forChild(".holding_condition"));
		}

	}

	public static boolean modify(Entity entity, BiPredicate<Instance, Context> tester) {

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			try {

				Context context = instance.createHolderContext(entity);

				if (VISITOR.push(instance) && tester.test(instance, context)) {
					return true;
				}

			}

			catch (PosUnloadedException | PosOutOfBoundsException ignored) {
				//  No-op; just need to soft error
			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

}
