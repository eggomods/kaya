package io.github.eggohito.kaya.mixin.impl.power.custom.callback_vibration_listener;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eggohito.kaya.power.custom.CallbackVibrationListenedPower;
import io.github.eggohito.kaya.power.entity.Powers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiConsumer;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class ServerLevelEntityCallbacksMixin {

	@WrapOperation(method = {"onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", "onSectionChange(Lnet/minecraft/world/entity/Entity;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateDynamicGameEventListener(Ljava/util/function/BiConsumer;)V"))
	void updatePowerVibrationListeners(Entity entity, BiConsumer<DynamicGameEventListener<?>, ServerLevel> updater, Operation<Void> original) {

		original.call(entity, updater);

		for (var instance : Powers.getInstances(entity, CallbackVibrationListenedPower.Instance.class)) {
			instance.update(entity, updater);
		}

	}

}
