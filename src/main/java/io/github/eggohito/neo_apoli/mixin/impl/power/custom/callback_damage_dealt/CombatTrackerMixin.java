package io.github.eggohito.neo_apoli.mixin.impl.power.custom.callback_damage_dealt;

import io.github.eggohito.neo_apoli.power.custom.CallbackDamageDealtPower;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CombatTracker.class)
public abstract class CombatTrackerMixin {

	@Shadow
	@Final
	private LivingEntity mob;

	@Inject(method = "recordDamage", at = @At("HEAD"))
	void invokeDamageDealtActions(DamageSource source, float amount, CallbackInfo ci) {
		CallbackDamageDealtPower.execute(source.getEntity(), this.mob, source, amount);
	}

}
