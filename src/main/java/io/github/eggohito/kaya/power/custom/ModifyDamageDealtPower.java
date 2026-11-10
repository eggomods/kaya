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

import java.util.List;

public record ModifyDamageDealtPower(List<Modifier> modifiers, Action onModifyAction, Properties properties) implements DamageModifyingPower {

	public static final MapCodec<ModifyDamageDealtPower> CODEC = DamageModifyingPower.codec(ModifyDamageDealtPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyDamageDealtPower> STREAM_CODEC = DamageModifyingPower.streamCodec(ModifyDamageDealtPower::new);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_DAMAGE_DEALT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends DamageModifyingPower.Instance<ModifyDamageDealtPower> {

		protected Instance(@NotNull ModifyDamageDealtPower power) {
			super(power);
		}

		@Override
		public Context createDamageContext(Entity actor, Entity target, DamageSource source, float amount) {
			return this.createHolderContextBuilder(actor)
				.withRequired(KayaContextParameters.ACTOR_ENTITY, actor)
				.withRequired(KayaContextParameters.TARGET_ENTITY, target)
				.withRequired(KayaContextParameters.DEALT_DAMAGE_SOURCE, source)
				.withRequired(KayaContextParameters.DEALT_DAMAGE_AMOUNT, amount)
				.withNullable(KayaContextParameters.DAMAGING_ENTITY, source.getEntity())
				.withNullable(KayaContextParameters.DIRECT_DAMAGING_ENTITY, source.getDirectEntity())
				.build(actor.level());
		}

	}

	public static float modify(@NotNull Entity actor, @NotNull Entity target, DamageSource source, float amount) {

		try (DamageModifyingPower.VISITOR) {
			return DamageModifyingPower.modify(KayaPowerTypes.MODIFY_DAMAGE_DEALT, Instance.class, actor, actor, target, source, amount);
		}

	}

}
