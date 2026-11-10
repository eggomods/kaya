package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.event.PowerModifyEvents;
import io.github.eggohito.kaya.modifier.Modifier;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ModifyFallingPower(List<Modifier> modifiers, BooleanProvider takeFallDamage, Properties properties) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyFallingPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyFallingPower::modifiers),
		BooleanProvider.CODEC.optionalFieldOf("take_fall_damage", new ConstantBooleanProvider(true)).forGetter(ModifyFallingPower::takeFallDamage),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyFallingPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyFallingPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(ObjectArrayList::new, Modifier.STREAM_CODEC), ModifyFallingPower::modifiers,
		BooleanProvider.STREAM_CODEC, ModifyFallingPower::takeFallDamage,
		Properties.STREAM_CODEC, ModifyFallingPower::properties,
		ModifyFallingPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_FALLING;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		Power.super.validate(validator);

		ContextValidatable.validate(modifiers(), validator, index -> ".modifiers[" + index + "]");
		takeFallDamage().validate(validator.forChild(".take_fall_damage"));

	}

	public static class Instance extends Power.Instance<ModifyFallingPower> {

		protected Instance(@NotNull ModifyFallingPower power) {
			super(power);
		}

		public List<Modifier> getModifiers() {
			return power.modifiers();
		}

		public List<Modifier.Operation> operations(Context context) {

			List<Modifier.Operation> result = new ObjectArrayList<>();
			MiscUtil.iterateList(power.modifiers(), (index, modifier) -> result.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));

			return result;

		}

		public boolean shouldNegateFallDamage(Context context) {
			return this.isActive(context)
				&& !power.takeFallDamage().getBoolean(context.forChild(".take_fall_damage"));
		}

	}

	public static boolean shouldNegateFallDamage(Entity entity) {

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			Context context = instance.createHolderContext(entity);

			try {

				if (VISITOR.push(instance) && instance.shouldNegateFallDamage(context)) {
					return true;
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

	public static double modify(Entity entity, double effectiveGravity) {

		List<Modifier.Operation> operations = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			Context context = instance.createHolderContext(entity);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					operations.addAll(instance.operations(context));
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(KayaPowerTypes.MODIFY_FALLING, operations, effectiveGravity);
		return Modifier.applyAll(operations, effectiveGravity);

	}

}
