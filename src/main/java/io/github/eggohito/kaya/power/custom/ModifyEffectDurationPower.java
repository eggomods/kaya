package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.event.PowerModifyEvents;
import io.github.eggohito.kaya.modifier.Modifier;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ModifyEffectDurationPower(List<Modifier> modifiers, Properties properties) implements Power {

	public static final MapCodec<ModifyEffectDurationPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyEffectDurationPower::modifiers),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyEffectDurationPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyEffectDurationPower> STREAM_CODEC = StreamCodec.composite(
		Modifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ModifyEffectDurationPower::modifiers,
		Properties.STREAM_CODEC, ModifyEffectDurationPower::properties,
		ModifyEffectDurationPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_EFFECT_DURATION;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<ModifyEffectDurationPower> {

		protected Instance(@NotNull ModifyEffectDurationPower power) {
			super(power);
		}

		public Context createContext(Entity holder, MobEffectInstance effectInstance, @Nullable Entity source) {
			return this.createHolderContextBuilder(holder)
				.withNullable(KayaContextParameters.ACTOR_ENTITY, source)
				.withRequired(KayaContextParameters.TARGET_ENTITY, holder)
				.withRequired(KayaContextParameters.APPLIED_EFFECT, effectInstance)
				.build(holder.level());
		}

		public List<Modifier.Operation> operations(Context context) {

			List<Modifier.Operation> result = new ObjectArrayList<>();
			MiscUtil.iterateList(power.modifiers(), (index, modifier) -> result.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));

			return result;

		}

	}

	public static int modify(Entity holder, MobEffectInstance effectInstance, @Nullable Entity source, int duration) {

		List<Modifier.Operation> operations = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(holder, Instance.class)) {

			Context context = instance.createContext(holder, effectInstance, source);

			if (instance.isActive(context)) {
				operations.addAll(instance.operations(context));
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(KayaPowerTypes.MODIFY_EFFECT_DURATION, operations, duration);
		return (int) Math.round(Modifier.applyAll(operations, duration));

	}

}
