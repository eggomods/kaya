package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.color.Color;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ModifyModelColorOtherPower(Color color, Properties properties) implements Power {

	public static final MapCodec<ModifyModelColorOtherPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.fieldOf("color").forGetter(ModifyModelColorOtherPower::color),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyModelColorOtherPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyModelColorOtherPower> STREAM_CODEC = StreamCodec.composite(
		Color.STREAM_CODEC, ModifyModelColorOtherPower::color,
		Properties.STREAM_CODEC, ModifyModelColorOtherPower::properties,
		ModifyModelColorOtherPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_MODEL_COLOR_OTHER;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		color().validate(validator.forChild(".color"));
	}

	public static class Instance extends Power.Instance<ModifyModelColorOtherPower> {

		protected Instance(@NotNull ModifyModelColorOtherPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @NotNull Entity rendered) {
			return this.createHolderContextBuilder(holder)
				.withRequired(KayaContextParameters.ACTOR_ENTITY, holder)
				.withRequired(KayaContextParameters.TARGET_ENTITY, rendered)
				.build(holder.level());
		}

		public int color(Context context) {
			return power.color().intValue(context.forChild(".color"));
		}

	}

	public static int modify(@NotNull Entity viewer, @NotNull Entity rendered, int color) {

		for (var instance : Powers.getInstances(viewer, Instance.class)) {

			Context context = instance.createContext(viewer, rendered);

			if (!Objects.equals(viewer, rendered) && instance.isActive(context)) {
				color = Color.mix(color, instance.color(context));
			}

		}

		return color;

	}

}
