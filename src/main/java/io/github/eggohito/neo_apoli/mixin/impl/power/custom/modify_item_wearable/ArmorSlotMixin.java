package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_item_wearable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eggohito.neo_apoli.power.custom.ModifyItemWearablePower;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class ArmorSlotMixin {

	@WrapOperation(method = "mayPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEquippableInSlot(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z"))
	boolean modifyItemWearableOnInsertOnArmorSlots(LivingEntity owner, ItemStack stack, EquipmentSlot slot, Operation<Boolean> original) {
		return ModifyItemWearablePower.modify(owner, stack, slot, () -> original.call(owner, stack, slot));
	}

}
