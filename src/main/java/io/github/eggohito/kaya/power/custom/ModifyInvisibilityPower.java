package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.condition.custom.ConstantCondition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

public record ModifyInvisibilityPower(Condition invisibleToCondition, BooleanProvider renderArmor, BooleanProvider renderOutline, Properties properties) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final BiPredicate<ModifyInvisibilityPower.Instance, Context> RENDER_OUTLINE = ModifyInvisibilityPower.Instance::isActiveAndShouldRenderOutline;
	public static final BiPredicate<ModifyInvisibilityPower.Instance, Context> RENDER_ARMOR = ModifyInvisibilityPower.Instance::isActiveAndShouldRenderArmor;

	public static final MapCodec<ModifyInvisibilityPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.optionalFieldOf("invisible_to_condition", new ConstantCondition(true)).forGetter(ModifyInvisibilityPower::invisibleToCondition),
		BooleanProvider.CODEC.optionalFieldOf("render_armor", new ConstantBooleanProvider(true)).forGetter(ModifyInvisibilityPower::renderArmor),
		BooleanProvider.CODEC.optionalFieldOf("render_outline", new ConstantBooleanProvider(true)).forGetter(ModifyInvisibilityPower::renderOutline),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyInvisibilityPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyInvisibilityPower> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, ModifyInvisibilityPower::invisibleToCondition,
		BooleanProvider.STREAM_CODEC, ModifyInvisibilityPower::renderArmor,
		BooleanProvider.STREAM_CODEC, ModifyInvisibilityPower::renderOutline,
		Properties.STREAM_CODEC, ModifyInvisibilityPower::properties,
		ModifyInvisibilityPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_INVISIBILITY;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		invisibleToCondition().validate(validator.forChild(".invisible_to_condition"));
		renderArmor().validate(validator.forChild(".render_armor"));
		renderOutline().validate(validator.forChild(".render_outline"));
	}

	public static class Instance extends Power.Instance<ModifyInvisibilityPower> {

		protected Instance(@NotNull ModifyInvisibilityPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @Nullable Entity viewer) {
			return this.createHolderContextBuilder(holder)
				.withNullable(KayaContextParameters.ACTOR_ENTITY, viewer)
				.withRequired(KayaContextParameters.TARGET_ENTITY, holder)
				.build(holder.level());
		}

		public boolean isInvisibleTo(Context context) {
			return this.isActive(context)
				&& power.invisibleToCondition().test(context.forChild(".invisible_to_condition"));
		}

		public boolean isActiveAndShouldRenderArmor(Context context) {
			return this.isActive(context)
				&& power.renderArmor().getBoolean(context.forChild(".render_armor"));
		}

		public boolean isActiveAndShouldRenderOutline(Context context) {
			return this.isActive(context)
				&& power.renderOutline().getBoolean(context.forChild(".render_outline"));
		}

	}

	public static boolean modify(Entity holder, @Nullable Entity viewer, BiPredicate<Instance, Context> tester, BooleanSupplier defaultValue) {

		for (var instance : Powers.getInstances(holder, Instance.class)) {

			Context context = instance.createContext(holder, viewer);

			try {

				if (VISITOR.push(instance) && tester.test(instance, context)) {
					return true;
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return defaultValue.getAsBoolean();

	}

}
