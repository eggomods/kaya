package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_item_wearable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eggohito.neo_apoli.duck.internal.EntityCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {

	@WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/EquipmentSlot$Type;HUMANOID_ARMOR:Lnet/minecraft/world/entity/EquipmentSlot$Type;", opcode = Opcodes.GETSTATIC)))
	boolean onArmorSlotOccupancyCheck(Slot slot, Operation<Boolean> original, @Local(ordinal = 1) ItemStack movingStack) {
		return original.call(slot)
			|| !slot.mayPlace(movingStack);
	}

	@WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/EquipmentSlot;OFFHAND:Lnet/minecraft/world/entity/EquipmentSlot;", opcode = Opcodes.GETSTATIC)))
	boolean onOffhandSlotOccupancyCheck(Slot slot, Operation<Boolean> original, @Local(ordinal = 1) ItemStack movingStack) {
		return original.call(slot)
			|| !slot.mayPlace(movingStack);
	}

	//  The addSlot call for adding the offhand slot
	@ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 1))
	Slot addOwnerToOffhandSlot(Slot original, @Local(argsOnly = true) Player owner) {

		if (original instanceof EntityCache entityCache) {
			entityCache.neo_apoli$setEntity(owner);
		}

		return original;

	}

}
