package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_item_wearable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eggohito.neo_apoli.power.custom.ModifyItemWearablePower;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

	@ModifyExpressionValue(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;mayPickup(Lnet/minecraft/world/entity/player/Player;)Z", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/ClickType;SWAP:Lnet/minecraft/world/inventory/ClickType;", opcode = Opcodes.GETSTATIC)))
	boolean modifyItemWearableOnOffHand(boolean original, int slotId, int button, @Local(argsOnly = true) Player player, @Local(ordinal = 1) ItemStack slotStack) {
		return button == 40 //  Check if the player pressed the swap offhand button
			? ModifyItemWearablePower.modify(player, slotStack, EquipmentSlot.OFFHAND, () -> original)
			: original;
	}

}
