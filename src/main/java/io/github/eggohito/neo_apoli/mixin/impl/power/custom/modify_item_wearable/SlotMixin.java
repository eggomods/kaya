package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_item_wearable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eggohito.neo_apoli.duck.internal.EntityCache;
import io.github.eggohito.neo_apoli.power.custom.ModifyItemWearablePower;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Slot.class)
public abstract class SlotMixin implements EntityCache {

	@Shadow
	@Final
	private int slot;

	@Shadow
	@Final
	public Container container;

	@Unique
	private final ThreadLocal<Entity> neo_apoli$owner = new ThreadLocal<>();

	@Override
	public @Nullable Entity neo_apoli$getEntity() {
		return neo_apoli$owner.get();
	}

	@Override
	public void neo_apoli$setEntity(@Nullable Entity entity) {

		if (entity == null) {
			this.neo_apoli$owner.remove();
		}

		else {
			this.neo_apoli$owner.set(entity);
		}

	}

	@ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
	boolean modifyItemWearableOnOffhand(boolean original, ItemStack stack) {
		return slot == 40 && container instanceof Inventory // Check if the slot is the offhand slot
			? ModifyItemWearablePower.modify(this.neo_apoli$getEntity(), stack, EquipmentSlot.OFFHAND, () -> original)
			: original;
	}

}
