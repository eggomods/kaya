package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public record CallbackDamageTakenPower(Optional<Condition> activeCondition, Action onHitAction, int priority) implements PrioritizedPower<CallbackDamageTakenPower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<CallbackDamageTakenPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Action.CODEC.fieldOf("on_hit_action").forGetter(CallbackDamageTakenPower::onHitAction))
		.and(Codec.INT.optionalFieldOf("priority", 0).forGetter(CallbackDamageTakenPower::priority))
		.apply(instance, CallbackDamageTakenPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackDamageTakenPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Action.STREAM_CODEC, CallbackDamageTakenPower::onHitAction,
		ByteBufCodecs.INT, CallbackDamageTakenPower::priority,
		CallbackDamageTakenPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.CALLBACK_DAMAGE_TAKEN;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		onHitAction().validate(validator.forChild(".on_hit_action"));
	}

	public static class Instance extends Power.Instance<CallbackDamageTakenPower> {

		protected Instance(@NotNull CallbackDamageTakenPower power) {
			super(power);
		}

		public Context createContext(@Nullable Entity actor, @NotNull Entity holder, DamageSource source, float amount) {
			return this.createHolderContextBuilder(holder)
				.withRequired(NeoApoliContextParameters.TAKEN_DAMAGE_SOURCE, source)
				.withRequired(NeoApoliContextParameters.TAKEN_DAMAGE_AMOUNT, amount)
				.withNullable(NeoApoliContextParameters.ACTOR_ENTITY, actor)
				.withRequired(NeoApoliContextParameters.TARGET_ENTITY, holder)
				.withNullable(NeoApoliContextParameters.DAMAGING_ENTITY, source.getEntity())
				.withNullable(NeoApoliContextParameters.DIRECT_DAMAGING_ENTITY, source.getDirectEntity())
				.build(holder.level());
		}

		public void execute(Context context) {
			power.onHitAction().execute(context.forChild(".on_hit_action"));
		}

	}

	public static void execute(Entity actor, Entity target, DamageSource damageSource, float damageAmount) {

		try (VISITOR) {

			for (var instance : new InstanceCollection<>(target, Instance.class, VISITOR::push)) {

				Context context = instance.createContext(actor, target, damageSource, damageAmount);

				if (instance.isActive(context)) {
					instance.execute(context);
				}

			}

		}

	}

}
