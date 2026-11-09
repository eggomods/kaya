package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.modifier.Modifier;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.DamageModifyingPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ModifyDamageTakenPower(List<Modifier> modifiers, Action onModifyAction, Properties properties) implements DamageModifyingPower {

	public static final MapCodec<ModifyDamageTakenPower> CODEC = DamageModifyingPower.codec(ModifyDamageTakenPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyDamageTakenPower> STREAM_CODEC = DamageModifyingPower.streamCodec(ModifyDamageTakenPower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_DAMAGE_TAKEN;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends DamageModifyingPower.Instance<ModifyDamageTakenPower> {

		protected Instance(@NotNull ModifyDamageTakenPower power) {
			super(power);
		}

		@Override
		public Context createDamageContext(@Nullable Entity actor, @NotNull Entity target, DamageSource source, float amount) {
			return this.createHolderContextBuilder(target)
				.withNullable(KayaContextParameters.ACTOR_ENTITY, actor)
				.withRequired(KayaContextParameters.TARGET_ENTITY, target)
				.withRequired(KayaContextParameters.TAKEN_DAMAGE_SOURCE, source)
				.withRequired(KayaContextParameters.TAKEN_DAMAGE_AMOUNT, amount)
				.withNullable(KayaContextParameters.DAMAGING_ENTITY, source.getEntity())
				.withNullable(KayaContextParameters.DIRECT_DAMAGING_ENTITY, source.getDirectEntity())
				.build(target.level());
		}

	}

	public static float modify(@NotNull Entity target, DamageSource source, float amount) {

		try (DamageModifyingPower.VISITOR) {
			return DamageModifyingPower.modify(KayaPowerTypes.MODIFY_DAMAGE_TAKEN, Instance.class, target, source.getEntity(), target, source, amount);
		}

	}

}
