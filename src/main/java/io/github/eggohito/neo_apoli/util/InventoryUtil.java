package io.github.eggohito.neo_apoli.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtil {

	public static SlotAccess createSingletonSlot(ItemStack stack) {
		return new SlotAccess() {

			ItemStack cachedStack = stack.copy();

			@Override
			public @NotNull ItemStack get() {
				return cachedStack;
			}

			@Override
			public boolean set(ItemStack stack) {
				this.cachedStack = stack.copy();
				return true;
			}

		};
	}

	public static void dropItem(ServerLevel serverWorld, Entity thrower, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
		dropItem(serverWorld, thrower, stack, throwRandomly, retainOwnership, 40);
	}

	public static void dropItem(ServerLevel serverWorld, Entity thrower, ItemStack stack, boolean throwRandomly, boolean retainOwnership, int pickupDelay) {

		if (stack.isEmpty()) {
			return;
		}

		float pitch = thrower.getXRot();
		float yaw = thrower.getYRot();

		RandomSource random = thrower.getRandom();
		double y = thrower.getEyeY() - 0.30000001192092896;

		ItemEntity itemEntity = new ItemEntity(serverWorld, thrower.getX(), y, thrower.getZ(), stack);
		itemEntity.setPickUpDelay(pickupDelay);

		if (retainOwnership) {
			itemEntity.setThrower(thrower);
		}

		float f, g;
		if (throwRandomly) {

			f = random.nextFloat() * 0.5F;
			g = random.nextFloat() * 6.2831855F;

			itemEntity.setDeltaMovement(-Mth.sin(g) * f, 0.20000000298023224, Mth.cos(g) * f);

		}

		else {

			f =  0.3F;
			g = Mth.sin(pitch * 0.017453292F);

			float h = Mth.cos(pitch * 0.017453292F);
			float i = Mth.sin(yaw * 0.017453292F);
			float j = Mth.cos(yaw * 0.017453292F);

			float k = random.nextFloat() * 6.2831855F;
			float l = 0.02F * random.nextFloat();

			itemEntity.setDeltaMovement((-i * h * f) + Math.cos(k) * l, -g * f + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F, (j * h * f) + Math.sin(k) * l);

		}

		serverWorld.tryAddFreshEntityWithPassengers(itemEntity);

	}

	@Nullable
	public static EquipmentSlot getEquipmentSlotFromStack(@Nullable LivingEntity entity, @NotNull ItemStack stack) {

		if (entity == null) {
			return null;
		}

		for (var slot : EquipmentSlot.values()) {

			if (ItemStack.matches(entity.getItemBySlot(slot), stack)) {
				return slot;
			}

		}

		return null;

	}

}
