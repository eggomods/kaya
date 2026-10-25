package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_item_wearable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.eggohito.neo_apoli.power.custom.ModifyItemWearablePower;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

	@Shadow
	public ServerPlayer player;

	@ModifyExpressionValue(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z", ordinal = 0))
	boolean modifyItemWearableOnOffhand(boolean original, ServerboundPlayerActionPacket packet) {
		return original
			|| !ModifyItemWearablePower.modify(this.player, this.player.getItemInHand(InteractionHand.MAIN_HAND), EquipmentSlot.OFFHAND, () -> true);
	}

}
