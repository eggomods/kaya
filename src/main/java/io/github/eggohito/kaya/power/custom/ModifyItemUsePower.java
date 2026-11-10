package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.CodecUtil;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.PriorityPhase;
import io.github.eggohito.kaya.util.StreamCodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record ModifyItemUsePower(Action onUseAction, InteractionResult result, EnumSet<InteractionHand> hands, TriggerType triggerType, int priority, Properties properties) implements PrioritizedPower<ModifyItemUsePower> {

	public static final MapCodec<ModifyItemUsePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Action.CODEC.fieldOf("on_use_action").forGetter(ModifyItemUsePower::onUseAction),
		KayaCodecs.INTERACTION_RESULT.optionalFieldOf("result", InteractionResult.SUCCESS).forGetter(ModifyItemUsePower::result),
		KayaCodecs.HAND_SET.optionalFieldOf("hands", EnumSet.allOf(InteractionHand.class)).forGetter(ModifyItemUsePower::hands),
		TriggerType.CODEC.fieldOf("trigger_type").forGetter(ModifyItemUsePower::triggerType),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyItemUsePower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyItemUsePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyItemUsePower> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC, ModifyItemUsePower::onUseAction,
		KayaStreamCodecs.INTERACTION_RESULT, ModifyItemUsePower::result,
		KayaStreamCodecs.HAND_SET, ModifyItemUsePower::hands,
		TriggerType.STREAM_CODEC, ModifyItemUsePower::triggerType,
		ByteBufCodecs.INT, ModifyItemUsePower::priority,
		Properties.STREAM_CODEC, ModifyItemUsePower::properties,
		ModifyItemUsePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_ITEM_USE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		onUseAction().validate(validator.forChild(".on_use_action"));
	}

	@Override
	public int priority() {
		return priority;
	}

	public static class Instance extends Power.Instance<ModifyItemUsePower> {

		protected Instance(@NotNull ModifyItemUsePower power) {
			super(power);
		}

		public Context createContext(Entity holder, SlotAccess slotAccess) {
			return this.createHolderContextBuilder(holder)
				.withRequired(KayaContextParameters.USED_ITEM_SLOT, slotAccess)
				.withRequired(KayaContextParameters.USED_ITEM, slotAccess.get())
				.build(holder.level());
		}

		public InteractionResult execute(Context context) {
			power.onUseAction().execute(context.forChild(".on_use_action"));
			return power.result();
		}

		public boolean doesApply(PriorityPhase priorityPhase, TriggerType triggerType, InteractionHand hand) {
			return power.inPhase(priorityPhase)
				&& power.hands().contains(hand)
				&& Objects.equals(power.triggerType(), triggerType);
		}

	}

	public static InteractionResult execute(Level world, LivingEntity user, InteractionHand hand, SlotAccess slotAccess, TriggerType triggerType, PriorityPhase priorityPhase, Consumer<InteractionResult> zeroPriorityResultSetter, Supplier<@Nullable InteractionResult> zeroPriorityResultGetter, Supplier<InteractionResult> defaultResultGetter) {
		InstanceCollection<Instance> instanceCollection = new InstanceCollection<>(user, Instance.class, instance -> instance.doesApply(priorityPhase, triggerType, hand));
		return switch (priorityPhase) {
			case BEFORE ->
				executeBeforeUse(instanceCollection, user, hand, slotAccess, zeroPriorityResultSetter, defaultResultGetter);
			case AFTER ->
				executeAfterUse(instanceCollection, world, user, hand, slotAccess, zeroPriorityResultGetter, defaultResultGetter);
		};
	}

	private static InteractionResult executeBeforeUse(InstanceCollection<Instance> instanceCollection, LivingEntity user, InteractionHand hand, SlotAccess slotAccess, Consumer<InteractionResult> zeroPriorityResultSetter, Supplier<InteractionResult> defaultResultGetter) {

		for (int priority = instanceCollection.getMaxPriority(); priority >= instanceCollection.getMinPriority() && instanceCollection.hasInstances(priority); priority--) {

			List<Instance> instances = instanceCollection.getInstances(priority);
			InteractionResult previousResult = InteractionResult.PASS;

			for (var instance: instances) {

				Context context = instance.createContext(user, slotAccess);

				if (instance.isActive(context)) {

					previousResult = instance.execute(context);

					if (!MiscUtil.isResultPass(previousResult)) {
						break;
					}

				}

			}

			boolean previousPassed = MiscUtil.isResultPass(previousResult);
			boolean zeroPriority = priority == 0;

			if (previousPassed || zeroPriority) {

				if (zeroPriority) {
					zeroPriorityResultSetter.accept(previousResult);
				}

				continue;

			}

			if (previousResult instanceof InteractionResult.Success(InteractionResult.SwingSource swingSource, InteractionResult.ItemContext ignored) && swingSource != InteractionResult.SwingSource.NONE) {
				user.swing(hand, swingSource == InteractionResult.SwingSource.SERVER);
			}

			return previousResult;

		}

		return defaultResultGetter.get();

	}

	private static InteractionResult executeAfterUse(InstanceCollection<Instance> instanceCollection, Level world, LivingEntity user, InteractionHand hand, SlotAccess slotAccess, Supplier<@Nullable InteractionResult> zeroPriorityResultGetter, Supplier<InteractionResult> defaultResultGetter) {

		InteractionResult original = defaultResultGetter.get();
		InteractionResult modified = InteractionResult.PASS;

		InteractionResult zeroPriorityResult = zeroPriorityResultGetter.get();

		if (zeroPriorityResult != null && !MiscUtil.isResultPass(zeroPriorityResult)) {
			modified = zeroPriorityResult;
		}

		else if (MiscUtil.isResultPass(original)) {

			for (int priority = instanceCollection.getMaxPriority(); priority >= instanceCollection.getMinPriority() && instanceCollection.hasInstances(priority); priority--) {

				List<Instance> instances = instanceCollection.getInstances(priority);
				InteractionResult previousResult = InteractionResult.PASS;

				for (var instance: instances) {

					Context context = instance.createContext(user, slotAccess);

					if (instance.isActive(context)) {

						previousResult = instance.execute(context);

						if (!MiscUtil.isResultPass(previousResult)) {
							break;
						}

					}

				}

				if (!MiscUtil.isResultPass(previousResult)) {
					modified = previousResult;
					break;
				}

			}

		}

		if (modified instanceof InteractionResult.Success(InteractionResult.SwingSource swingSource, InteractionResult.ItemContext ignored) && swingSource != InteractionResult.SwingSource.NONE) {
			user.swing(hand, swingSource == InteractionResult.SwingSource.SERVER);
		}

		return MiscUtil.overrideResult(original, modified);

	}

	public enum TriggerType implements StringRepresentable {

		INSTANT("instant"),
		START("start"),
		STOP("stop"),
		FINISH("finish"),
		DURING("during");

		public static final Codec<TriggerType> CODEC = CodecUtil.enumType(TriggerType.class);
		public static final StreamCodec<ByteBuf, TriggerType> STREAM_CODEC = StreamCodecUtil.enumType(TriggerType.class);

		private final String name;
		TriggerType(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

	}

}
