package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.event.PowerModifyEvents;
import io.github.eggohito.kaya.modifier.Modifier;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
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

public record ModifyAirSpeedPower(List<Modifier> modifiers, Properties properties) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyAirSpeedPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyAirSpeedPower::modifiers),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyAirSpeedPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyAirSpeedPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(ObjectArrayList::new, Modifier.STREAM_CODEC), ModifyAirSpeedPower::modifiers,
		Properties.STREAM_CODEC, ModifyAirSpeedPower::properties,
		ModifyAirSpeedPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_AIR_SPEED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		MiscUtil.iterateList(modifiers(), (index, modifier) -> modifier.validate(validator.forChild(".modifiers[" + index + "]")));
	}

	public static class Instance extends Power.Instance<ModifyAirSpeedPower> {

		protected Instance(@NotNull ModifyAirSpeedPower power) {
			super(power);
		}

		public List<Modifier> getModifiers() {
			return power.modifiers();
		}

	}

	public static float modify(Entity entity, float airSpeed) {

		List<Modifier.Operation> entries = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			Context context = instance.createHolderContext(entity);

			try {

				if (VISITOR.push(instance) && instance.isActive(context)) {
					MiscUtil.iterateList(instance.getModifiers(), (index, modifier) -> entries.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(KayaPowerTypes.MODIFY_AIR_SPEED, entries, airSpeed);
		return (float) Modifier.applyAll(entries, airSpeed);

	}

}
