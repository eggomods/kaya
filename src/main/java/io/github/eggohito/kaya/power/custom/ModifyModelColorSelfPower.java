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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ModifyModelColorSelfPower(Color color, Properties properties) implements Power {

	public static final MapCodec<ModifyModelColorSelfPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.fieldOf("color").forGetter(ModifyModelColorSelfPower::color),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyModelColorSelfPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyModelColorSelfPower> STREAM_CODEC = StreamCodec.composite(
		Color.STREAM_CODEC, ModifyModelColorSelfPower::color,
		Properties.STREAM_CODEC, ModifyModelColorSelfPower::properties,
		ModifyModelColorSelfPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_MODEL_COLOR_SELF;
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

	public static class Instance extends Power.Instance<ModifyModelColorSelfPower> {

		protected Instance(@NotNull ModifyModelColorSelfPower power) {
			super(power);
		}

		public Context createContext(@NotNull Entity holder, @Nullable Entity viewer) {
			return this.createHolderContextBuilder(holder)
				.withNullable(KayaContextParameters.ACTOR_ENTITY, viewer)
				.withRequired(KayaContextParameters.TARGET_ENTITY, holder)
				.build(holder.level());
		}

		public int getColor(Context context) {
			return power.color().intValue(context.forChild(".color"));
		}

	}

	public static int modify(@Nullable Entity viewer, @NotNull Entity rendered, int color) {

		for (var instance : Powers.getInstances(rendered, Instance.class)) {

			Context context = instance.createContext(rendered, viewer);

			if (viewer == null || Objects.equals(viewer, rendered) || instance.isActive(context)) {
				color = Color.mix(color, instance.getColor(context));
			}

		}

		return color;

	}

}
