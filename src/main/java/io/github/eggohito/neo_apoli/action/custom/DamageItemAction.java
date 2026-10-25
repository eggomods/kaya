package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.slot.SlotProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.InventoryUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.Consumers;

import java.util.Optional;

public record DamageItemAction(IntProvider amount, SlotProvider slot, BooleanProvider ignoreEnchantments, Optional<EntityProvider> itemHolder) implements Action {

	public static final MapCodec<DamageItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.optionalFieldOf("amount", new ConstantIntProvider(1)).forGetter(DamageItemAction::amount),
		SlotProvider.CODEC.fieldOf("slot").forGetter(DamageItemAction::slot),
		BooleanProvider.CODEC.optionalFieldOf("ignore_enchantments", new ConstantBooleanProvider(false)).forGetter(DamageItemAction::ignoreEnchantments),
		EntityProvider.CODEC.optionalFieldOf("item_holder").forGetter(DamageItemAction::itemHolder)
	).apply(instance, DamageItemAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DamageItemAction> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, DamageItemAction::amount,
		SlotProvider.STREAM_CODEC, DamageItemAction::slot,
		BooleanProvider.STREAM_CODEC, DamageItemAction::ignoreEnchantments,
		ByteBufCodecs.optional(EntityProvider.STREAM_CODEC), DamageItemAction::itemHolder,
		DamageItemAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.DAMAGE_ITEM;
	}

	@Override
	public void execute(Context context) {

		if (!(context.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		ItemStack stack = slot().getValue(context.forChild(".slot"))
			.map(SlotAccess::get)
			.orElse(ItemStack.EMPTY);

		if (stack.isEmpty() || !stack.isDamageableItem()) {
			return;
		}

		boolean ignoreEnchantments = ignoreEnchantments().getBoolean(context.forChild(".ignore_enchantments"));
		int amount = Math.abs(amount().getInt(context.forChild(".amount")));

		ServerPlayer itemHolder = itemHolder()
			.flatMap(self -> self.getValue(context.forChild(".entity")))
			.filter(ServerPlayer.class::isInstance)
			.map(ServerPlayer.class::cast)
			.orElse(null);

		if (ignoreEnchantments) {

			EquipmentSlot slot = InventoryUtil.getEquipmentSlotFromStack(itemHolder, stack);
			int appliedDamage = stack.getDamageValue() + amount;

			if (itemHolder != null) {

				if (itemHolder.hasInfiniteMaterials()) {
					return;
				}

				CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(itemHolder, stack, appliedDamage);

			}

			stack.setDamageValue(appliedDamage);

			if (stack.isBroken()) {

				Item item = stack.getItem();
				stack.shrink(1);

				if (itemHolder != null && slot != null) {
					itemHolder.onEquippedItemBroken(item, slot);
				}

			}

		}

		else {
			stack.hurtAndBreak(amount, serverLevel, itemHolder, Consumers.nop());
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		amount().validate(validator.forChild(".amount"));
		slot().validate(validator.forChild(".slot"));
		ignoreEnchantments().validate(validator.forChild(".ignore_enchantments"));
		itemHolder().ifPresent(p -> p.validate(validator.forChild(".item_holder")));
	}

}
