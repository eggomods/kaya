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
import org.jetbrains.annotations.Nullable;


public record CallbackDamageTakenPower(Action onHitAction, int priority, Properties properties) implements PrioritizedPower<CallbackDamageTakenPower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<CallbackDamageTakenPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Action.CODEC.fieldOf("on_hit_action").forGetter(CallbackDamageTakenPower::onHitAction),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(CallbackDamageTakenPower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, CallbackDamageTakenPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackDamageTakenPower> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC, CallbackDamageTakenPower::onHitAction,
		ByteBufCodecs.INT, CallbackDamageTakenPower::priority,
		Properties.STREAM_CODEC, CallbackDamageTakenPower::properties,
		CallbackDamageTakenPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_DAMAGE_TAKEN;
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
				.withRequired(KayaContextParameters.TAKEN_DAMAGE_SOURCE, source)
				.withRequired(KayaContextParameters.TAKEN_DAMAGE_AMOUNT, amount)
				.withNullable(KayaContextParameters.ACTOR_ENTITY, actor)
				.withRequired(KayaContextParameters.TARGET_ENTITY, holder)
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

			for (var instance : new InstanceCollection<>(target, Instance.class, VISITOR::push)) {

				Context context = instance.createContext(actor, target, damageSource, damageAmount);

				if (instance.isActive(context)) {
					instance.execute(context);
				}

			}

		}

	}

}
