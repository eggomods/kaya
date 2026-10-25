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

import java.util.Optional;


public record CallbackDamageDealtPower(Optional<Condition> activeCondition, Action onHitAction, int priority) implements PrioritizedPower<CallbackDamageDealtPower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<CallbackDamageDealtPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Action.CODEC.fieldOf("on_hit_action").forGetter(CallbackDamageDealtPower::onHitAction))
		.and(Codec.INT.optionalFieldOf("priority", 0).forGetter(CallbackDamageDealtPower::priority))
		.apply(instance, CallbackDamageDealtPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackDamageDealtPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Action.STREAM_CODEC, CallbackDamageDealtPower::onHitAction,
		ByteBufCodecs.INT, CallbackDamageDealtPower::priority,
		CallbackDamageDealtPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.CALLBACK_DAMAGE_DEALT;
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

	public static class Instance extends Power.Instance<CallbackDamageDealtPower> {

		protected Instance(@NotNull CallbackDamageDealtPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @NotNull Entity target, DamageSource source, float amount) {
			return this.createHolderContextBuilder(holder)
				.withRequired(NeoApoliContextParameters.DEALT_DAMAGE_SOURCE, source)
				.withRequired(NeoApoliContextParameters.DEALT_DAMAGE_AMOUNT, amount)
				.withRequired(NeoApoliContextParameters.ACTOR_ENTITY, holder)
				.withRequired(NeoApoliContextParameters.TARGET_ENTITY, target)
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

			for (var instance : new InstanceCollection<>(actor, Instance.class, VISITOR::push)) {

				Context context = instance.createContext(actor, target, damageSource, damageAmount);

				if (instance.isActive(context)) {
					instance.execute(context);
				}

			}

		}

	}

}
