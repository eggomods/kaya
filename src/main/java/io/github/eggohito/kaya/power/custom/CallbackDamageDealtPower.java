package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;


public record CallbackDamageDealtPower(Action onHitAction, int priority, Properties properties) implements PrioritizedPower<CallbackDamageDealtPower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<CallbackDamageDealtPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Action.CODEC.fieldOf("on_hit_action").forGetter(CallbackDamageDealtPower::onHitAction),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(CallbackDamageDealtPower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, CallbackDamageDealtPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackDamageDealtPower> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC, CallbackDamageDealtPower::onHitAction,
		ByteBufCodecs.INT, CallbackDamageDealtPower::priority,
		Properties.STREAM_CODEC, CallbackDamageDealtPower::properties,
		CallbackDamageDealtPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_DAMAGE_DEALT;
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
				.withRequired(KayaContextParameters.DEALT_DAMAGE_SOURCE, source)
				.withRequired(KayaContextParameters.DEALT_DAMAGE_AMOUNT, amount)
				.withRequired(KayaContextParameters.ACTOR_ENTITY, holder)
				.withRequired(KayaContextParameters.TARGET_ENTITY, target)
				.withNullable(KayaContextParameters.DAMAGING_ENTITY, source.getEntity())
				.withNullable(KayaContextParameters.DIRECT_DAMAGING_ENTITY, source.getDirectEntity())
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
