package io.github.eggohito.neo_apoli.power.custom.misc;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.hud.element.HudElement;
import io.github.eggohito.neo_apoli.hud.element.NumberBoundHudElement;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface CooldownPower extends Power {

	HudElement hudElement();

	IntProvider cooldown();

	@Override
	default void validate(ContextValidator validator) {
		Power.super.validate(validator);
		hudElement().validate(validator.forChild(".hud_element"));
		cooldown().validate(validator.forChild(".cooldown"));
	}

	static <P extends CooldownPower> Products.P2<RecordCodecBuilder.Mu<P>, HudElement, IntProvider> addFields(RecordCodecBuilder.Instance<P> instance) {
		return instance.group(
			HudElement.CODEC.fieldOf("hud_element").forGetter(CooldownPower::hudElement),
			IntProvider.CODEC.fieldOf("cooldown").forGetter(CooldownPower::cooldown)
		);
	}

	class Instance<P extends CooldownPower> extends Power.Instance<P> {

		protected static final MapCodec<Long> LAST_USE_TIME_CODEC = Codec.LONG.fieldOf("last_use_time");
		protected long lastUseTime;

		public Instance(@NotNull P power) {
			super(power);
		}

		@Override
		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {
			return LAST_USE_TIME_CODEC.decode(ops, mapInput)
				.map(value -> this.lastUseTime = value)
				.map(ignored -> Unit.INSTANCE);
		}

		@Override
		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {
			return LAST_USE_TIME_CODEC.encode(this.lastUseTime, ops, prefix);
		}

		public HudElement hudElement() {
			return power.hudElement();
		}

		public IntProvider cooldown() {
			return power.cooldown();
		}

		public Context createContext(Entity holder) {

			Context holderContext = this.createHolderContext(holder);

			return new Context.Builder(holderContext)
				.withRequired(NumberBoundHudElement.CURRENT_VALUE, (float) this.getRemainingTicks(holderContext))
				.withRequired(NumberBoundHudElement.MIN_VALUE, 0.0F)
				.withRequired(NumberBoundHudElement.MAX_VALUE, (float) cooldown().getInt(holderContext))
				.build(holderContext.level());

		}

		public boolean shouldRender(Context context, HudElement.RenderPhase renderPhase) {

			long timePassed = context.level().getGameTime() - lastUseTime;
			int cooldown = power.cooldown().getInt(context.forChild(".cooldown"));

			return timePassed <= cooldown
				&& hudElement().shouldRender(context, renderPhase);

		}

		public double getProgress(Context context) {

			double timePassed = context.level().getGameTime() - lastUseTime;
			double progress = timePassed / (double) cooldown().getInt(context.forChild(".cooldown"));

			return Mth.clamp(progress, 0D, 1D);

		}

		public int getRemainingTicks(Context context) {

			long timePassed = context.level().getGameTime() - lastUseTime;
			long remainingTicks = cooldown().getInt(context.forChild(".cooldown")) - timePassed;

			return (int) Math.max(0, remainingTicks);

		}

		public void trigger(Entity holder) {

			if (holder.level().isClientSide()) {
				return;
			}

			this.lastUseTime = holder.level().getGameTime();
			this.syncData(holder);

		}

	}

}
