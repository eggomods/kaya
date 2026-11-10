package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public record ModifyElytraFlightPower(BooleanProvider allow, int priority, Properties properties) implements PrioritizedPower<ModifyElytraFlightPower> {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyElytraFlightPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BooleanProvider.CODEC.fieldOf("allow").forGetter(ModifyElytraFlightPower::allow),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyElytraFlightPower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyElytraFlightPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyElytraFlightPower> STREAM_CODEC = StreamCodec.composite(
		BooleanProvider.STREAM_CODEC, ModifyElytraFlightPower::allow,
		ByteBufCodecs.INT, ModifyElytraFlightPower::priority,
		Properties.STREAM_CODEC, ModifyElytraFlightPower::properties,
		ModifyElytraFlightPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_ELYTRA_FLIGHT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		allow().validate(validator.forChild(".allow"));
	}

	public static class Instance extends Power.Instance<ModifyElytraFlightPower> {

		protected Instance(@NotNull ModifyElytraFlightPower power) {
			super(power);
		}

		public boolean isAllowed(Context context) {
			return power.allow().getBoolean(context.forChild(".allow"));
		}

	}

	public static boolean modify(Entity entity, BooleanSupplier defaultValue) {

		for (var instance : new InstanceCollection<>(entity, Instance.class)) {

			Context context = instance.createHolderContext(entity);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					return instance.isAllowed(context);
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return defaultValue.getAsBoolean();

	}

}
