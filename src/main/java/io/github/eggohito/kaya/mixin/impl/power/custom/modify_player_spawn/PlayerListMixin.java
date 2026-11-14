package io.github.eggohito.kaya.mixin.impl.power.custom.modify_player_spawn;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.eggohito.kaya.power.custom.ModifyPlayerSpawnPower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

	@ModifyExpressionValue(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"))
	TeleportTransition onRespawn(TeleportTransition original, ServerPlayer player) {

		if (player.getRespawnConfig() != null && !original.missingRespawnBlock()) {
			return original;
		}

		else {
			return ModifyPlayerSpawnPower.getSpawnLocationOrElse(player, () -> original);
		}

	}

}
