package io.github.eggohito.neo_apoli.mixin.impl.power.custom.callback_damage_taken;

import io.github.eggohito.neo_apoli.power.custom.CallbackDamageTakenPower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntity {

	ArmorStandMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "hurtServer", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;isCreativePlayer()Z")))
	void invokeDamageTakenActions(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

		if (cir.getReturnValueZ() && source.getEntity() != null) {
			CallbackDamageTakenPower.execute(source.getEntity(), this, source, amount);
		}

	}

}
