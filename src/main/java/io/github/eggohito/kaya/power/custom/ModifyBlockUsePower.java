package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.action.custom.NothingAction;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.parameter.BlockContextParameter;
import io.github.eggohito.kaya.context.parameter.DirectionContextParameter;
import io.github.eggohito.kaya.exception.PosOutOfBoundsException;
import io.github.eggohito.kaya.exception.PosUnloadedException;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.BlockUsePhase;
import io.github.eggohito.kaya.util.CachedBlock;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.PriorityPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record ModifyBlockUsePower(Actions actions, Conditions conditions, EnumSet<BlockUsePhase> usePhases, int priority, Properties properties) implements PrioritizedPower<ModifyBlockUsePower> {

	public static final BlockContextParameter USED_BLOCK = new BlockContextParameter(Kaya.id("used_block"));
	public static final DirectionContextParameter USED_SIDE = new DirectionContextParameter(Kaya.id("used_side"));

	public static final MapCodec<ModifyBlockUsePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Actions.CODEC.forGetter(ModifyBlockUsePower::actions),
		Conditions.CODEC.forGetter(ModifyBlockUsePower::conditions),
		BlockUsePhase.SET_CODEC.optionalFieldOf("use_phases", EnumSet.allOf(BlockUsePhase.class)).forGetter(ModifyBlockUsePower::usePhases),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyBlockUsePower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyBlockUsePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyBlockUsePower> STREAM_CODEC = StreamCodec.composite(
		Actions.STREAM_CODEC, ModifyBlockUsePower::actions,
		Conditions.STREAM_CODEC, ModifyBlockUsePower::conditions,
		BlockUsePhase.SET_STREAM_CODEC, ModifyBlockUsePower::usePhases,
		ByteBufCodecs.INT, ModifyBlockUsePower::priority,
		Properties.STREAM_CODEC, ModifyBlockUsePower::properties,
		ModifyBlockUsePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_BLOCK_USE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		actions().validate(validator);
		conditions().validate(validator);
	}

	public static class Instance extends Power.Instance<ModifyBlockUsePower> {

		protected Instance(@NotNull ModifyBlockUsePower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockHitResult blockResult, InteractionHand hand) {

			Level level = holder.level();
			BlockPos blockPos  = blockResult.getBlockPos();
			SlotAccess usedItemSlot = holder instanceof LivingEntity livingEntity
				? SlotAccess.of(() -> livingEntity.getItemInHand(hand), stack -> livingEntity.setItemInHand(hand, stack))
				: SlotAccess.NULL;

			return this.createHolderContextBuilder(holder)
				.withRequired(USED_BLOCK, CachedBlock.fromLoadedPos(level, blockPos))
				.withRequired(USED_SIDE, blockResult.getDirection())
				.withRequired(KayaContextParameters.USED_ITEM_SLOT, usedItemSlot)
				.withRequired(KayaContextParameters.USED_ITEM, usedItemSlot.get())
				.build(level);

		}

		public InteractionResult apply(Context context) {
			return power.actions().execute(context);
		}

		public boolean doesApply(BlockUsePhase interactionPhase, PriorityPhase priorityPhase) {
			return power.usePhases().contains(interactionPhase)
				&& power.inPhase(priorityPhase);
		}

	}

	public record Actions(Action action, InteractionResult result) implements ContextUser {

		public static final MapCodec<Actions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Action.CODEC.optionalFieldOf("action", NothingAction.INSTANCE).forGetter(Actions::action),
			KayaCodecs.INTERACTION_RESULT.optionalFieldOf("result", InteractionResult.SUCCESS).forGetter(Actions::result)
		).apply(instance, Actions::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Actions> STREAM_CODEC = StreamCodec.composite(
			Action.STREAM_CODEC, Actions::action,
			KayaStreamCodecs.INTERACTION_RESULT, Actions::result,
			Actions::new
		);

		@Override
		public void validate(ContextValidator validator) {
			ContextUser.super.validate(validator);
			action().validate(validator.forChild(".action"));
		}

		public InteractionResult execute(Context context) {
			action().execute(context.forChild(".action"));
			return result();
		}

	}

	public record Conditions(EnumSet<Direction> directions, EnumSet<InteractionHand> hands) implements ContextUser {

		public static final MapCodec<Conditions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			KayaCodecs.DIRECTION_SET.optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(Conditions::directions),
			KayaCodecs.HAND_SET.optionalFieldOf("hands", EnumSet.allOf(InteractionHand.class)).forGetter(Conditions::hands)
		).apply(instance, Conditions::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Conditions> STREAM_CODEC = StreamCodec.composite(
			KayaStreamCodecs.DIRECTION_SET, Conditions::directions,
			KayaStreamCodecs.HAND_SET, Conditions::hands,
			Conditions::new
		);

		public boolean test(Context context, InteractionHand hand) {
			return context.getOptional(USED_SIDE).map(directions()::contains).orElse(false)
				&& hands().contains(hand);
		}

	}

	public static InteractionResult execute(Player player, InteractionHand hand, BlockHitResult blockHitResult, BlockUsePhase interactionPhase, PriorityPhase priorityPhase, Consumer<InteractionResult> zeroPriorityResultSetter, Supplier<InteractionResult> zeroPriorityResultGetter, Supplier<InteractionResult> defaultValueSupplier) {
		return switch (priorityPhase) {
			case BEFORE ->
				executeOnBeforeBlockUse(player, hand, blockHitResult, interactionPhase, zeroPriorityResultSetter, defaultValueSupplier);
			case AFTER ->
				executeOnAfterBlockUse(player, hand, blockHitResult, interactionPhase, zeroPriorityResultGetter, defaultValueSupplier);
		};
	}

	private static InteractionResult executeOnBeforeBlockUse(Player player, InteractionHand hand, BlockHitResult blockHitResult, BlockUsePhase interactionPhase, Consumer<InteractionResult> zeroPriorityResultSetter, Supplier<InteractionResult> defaultResultSupplier) {

		InstanceCollection<Instance> instanceCollection = new InstanceCollection<>(player, Instance.class, instance -> instance.doesApply(interactionPhase, PriorityPhase.BEFORE));

		for (int priority = instanceCollection.getMaxPriority(); priority >= instanceCollection.getMinPriority(); priority--) {

			if (!instanceCollection.hasInstances(priority)) {
				continue;
			}

			List<Instance> instances = instanceCollection.getInstances(priority);
			InteractionResult previousResult = InteractionResult.PASS;

			for (var instance : instances) {

				try {

					Context context = instance.createContext(player, blockHitResult, hand);

					if (instance.isActive(context)) {
						previousResult = MiscUtil.overrideResult(previousResult, instance.apply(context));
					}

				}

				catch (PosUnloadedException | PosOutOfBoundsException ignored) {
					//  No-op; just need to soft error
				}

			}

			boolean previousPassed = previousResult == InteractionResult.PASS;
			boolean zeroPriority = priority == 0;

			if (previousPassed || zeroPriority) {

				if (zeroPriority) {
					zeroPriorityResultSetter.accept(previousResult);
				}

				continue;

			}

			if (previousResult instanceof InteractionResult.Success(InteractionResult.SwingSource swingSource, InteractionResult.ItemContext ignored) && swingSource != InteractionResult.SwingSource.NONE) {
				player.swing(hand, swingSource == InteractionResult.SwingSource.SERVER);
			}

			return previousResult;

		}

		return defaultResultSupplier.get();

	}

	private static InteractionResult executeOnAfterBlockUse(Player player, InteractionHand hand, BlockHitResult blockHitResult, BlockUsePhase interactionPhase, Supplier<InteractionResult> zeroPriorityResultGetter, Supplier<InteractionResult> defaultResultSupplier) {

		InteractionResult original = defaultResultSupplier.get();
		InteractionResult modified = InteractionResult.PASS;

		InteractionResult zeroPriorityResult = zeroPriorityResultGetter.get();

		if (zeroPriorityResult != null && zeroPriorityResult != InteractionResult.PASS) {
			modified = zeroPriorityResult;
		}

		else if (original == InteractionResult.PASS) {

			InstanceCollection<Instance> instanceCollection = new InstanceCollection<>(player, Instance.class, instance -> instance.doesApply(interactionPhase, PriorityPhase.AFTER));

			for (int priority = instanceCollection.getMaxPriority(); priority >= instanceCollection.getMinPriority(); priority--) {

				if (!instanceCollection.hasInstances(priority)) {
					continue;
				}

				List<Instance> instances = instanceCollection.getInstances(priority);
				InteractionResult previousResult = InteractionResult.PASS;

				for (var instance : instances) {

					try {

						Context context = instance.createContext(player, blockHitResult, hand);

						if (instance.isActive(context)) {
							previousResult = MiscUtil.overrideResult(previousResult, instance.apply(context));
						}

					}

					catch (PosUnloadedException | PosOutOfBoundsException ignored) {
						//  No-op; just need to soft error
					}

				}

				if (previousResult != InteractionResult.PASS) {
					modified = previousResult;
					break;
				}

			}

		}

		if (modified instanceof InteractionResult.Success(InteractionResult.SwingSource swingSource, InteractionResult.ItemContext ignored) && swingSource != InteractionResult.SwingSource.NONE) {
			player.swing(hand, swingSource == InteractionResult.SwingSource.SERVER);
		}

		return MiscUtil.overrideResult(original, modified);

	}

}
