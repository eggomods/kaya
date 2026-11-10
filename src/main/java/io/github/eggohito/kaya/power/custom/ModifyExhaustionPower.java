package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
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
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ModifyExhaustionPower(List<Modifier> modifiers, Properties properties) implements Power {

	public static final MapCodec<ModifyExhaustionPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyExhaustionPower::modifiers),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyExhaustionPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyExhaustionPower> STREAM_CODEC = StreamCodec.composite(
		Modifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ModifyExhaustionPower::modifiers,
		Properties.STREAM_CODEC, ModifyExhaustionPower::properties,
		ModifyExhaustionPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_EXHAUSTION;
	}

	@Override
	public Instance createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		ContextValidatable.validate(modifiers(), validator, index -> ".modifiers[" + index + "]");
	}

	public static class Instance extends Power.Instance<ModifyExhaustionPower> {

		protected Instance(@NotNull ModifyExhaustionPower power) {
			super(power);
		}

		public List<Modifier> modifiers() {
			return power.modifiers();
		}

	}

	public static float modify(Player player, float exhaustion) {

		List<Modifier.Operation> operations = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(player, Instance.class)) {

			Context context = instance.createHolderContext(player);

			if (instance.isActive(context)) {
				MiscUtil.iterateList(instance.modifiers(), (index, modifier) -> operations.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(KayaPowerTypes.MODIFY_EXHAUSTION, operations, exhaustion);
		return (float) Modifier.applyAll(operations, exhaustion);

	}

}
