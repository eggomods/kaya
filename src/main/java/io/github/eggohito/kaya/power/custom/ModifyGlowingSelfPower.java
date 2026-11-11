package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.color.Color;
import io.github.eggohito.kaya.color.custom.Argb;
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

public record ModifyGlowingSelfPower(BooleanProvider useTeamColors, Color color, Properties properties) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyGlowingSelfPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BooleanProvider.CODEC.optionalFieldOf("use_team_color", new ConstantBooleanProvider(true)).forGetter(ModifyGlowingSelfPower::useTeamColors),
		Color.CODEC.optionalFieldOf("color", Argb.DEFAULT).forGetter(ModifyGlowingSelfPower::color),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyGlowingSelfPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyGlowingSelfPower> STREAM_CODEC = StreamCodec.composite(
		BooleanProvider.STREAM_CODEC, ModifyGlowingSelfPower::useTeamColors,
		Color.STREAM_CODEC, ModifyGlowingSelfPower::color,
		Properties.STREAM_CODEC, ModifyGlowingSelfPower::properties,
		ModifyGlowingSelfPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_GLOWING_SELF;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		Power.super.validate(validator);

		useTeamColors().validate(validator.forChild(".use_team_color"));
		color().validate(validator.forChild(".color"));

	}

	public static class Instance extends Power.Instance<ModifyGlowingSelfPower> {

		protected Instance(@NotNull ModifyGlowingSelfPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @Nullable Entity viewer) {
			return this.createHolderContextBuilder(holder)
				.withNullable(KayaContextParameters.ACTOR_ENTITY, viewer)
				.withRequired(KayaContextParameters.TARGET_ENTITY, holder)
				.build(holder.level());
		}

		public boolean doesApply(Context context, boolean hasTeamColor) {
			return this.isActive(context)
				&& (!hasTeamColor || !this.shouldUseTeamColor(context));
		}

		public boolean shouldUseTeamColor(Context context) {
			return power.useTeamColors().getBoolean(context.forChild(".use_team_color"));
		}

		public int color(Context context) {
			return power.color().intValue(context.forChild(".color"));
		}

	}

	public static boolean modifyGlowing(Entity viewer, @NotNull Entity rendered) {

		for (var instance : Powers.getInstances(rendered, Instance.class)) {

			Context context = instance.createContext(rendered, viewer);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					return true;
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

	public static int modifyColor(Entity viewer, @NotNull Entity rendered, boolean hasTeamColor, int color) {

		for (var instance : Powers.getInstances(rendered, Instance.class)) {

			Context context = instance.createContext(rendered, viewer);

			try {

				if (VISITOR.push(instance) && instance.doesApply(context, hasTeamColor)) {
					color = Color.mix(color, instance.color(context));
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return color;

	}

}
