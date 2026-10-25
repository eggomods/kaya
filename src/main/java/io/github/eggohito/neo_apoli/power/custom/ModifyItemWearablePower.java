package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.ItemContextParameter;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.util.InventoryUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public record ModifyItemWearablePower(EnumMap<EquipmentSlot, Condition> slots) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();
	public static final ItemContextParameter WORN_ITEM = new ItemContextParameter(NeoApoli.id("worn_item"));

	private static final Codec<EquipmentSlot> NO_MAINHAND_SLOT_CODEC = EquipmentSlot.CODEC.comapFlatMap(
		slot -> slot == EquipmentSlot.MAINHAND
			? DataResult.error(() -> "Equipment slot 'mainhand' is not allowed!")
			: DataResult.success(slot),
		Function.identity()
	);

	private static final Codec<EnumMap<EquipmentSlot, Condition>> SLOTS_CODEC = ExtraCodecs.nonEmptyMap(Codec.unboundedMap(NO_MAINHAND_SLOT_CODEC, Condition.CODEC)).xmap(EnumMap::new, Function.identity());
	private static final StreamCodec<RegistryFriendlyByteBuf, EnumMap<EquipmentSlot, Condition>> SLOTS_STREAM_CODEC = ByteBufCodecs.map(ignored -> new EnumMap<>(EquipmentSlot.class), EquipmentSlot.STREAM_CODEC, Condition.STREAM_CODEC);

	public static final MapCodec<ModifyItemWearablePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(SLOTS_CODEC.fieldOf("slots").forGetter(ModifyItemWearablePower::slots))
		.apply(instance, ModifyItemWearablePower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyItemWearablePower> STREAM_CODEC = StreamCodec.composite(
		SLOTS_STREAM_CODEC, ModifyItemWearablePower::slots,
		ModifyItemWearablePower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_ITEM_WEARABLE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		Power.super.validate(validator);
		ContextValidator slotsValidator = validator.forChild(".slots");

		this.slots().forEach((slot, entry) -> entry.validate(slotsValidator.forChild("." + slot.getSerializedName())));

	}

	public static class Instance extends Power.Instance<ModifyItemWearablePower> {

		protected Instance(@NotNull ModifyItemWearablePower power) {
			super(power);
		}

		@Override
		public void onGranted(Entity holder) {

			if (!(holder instanceof LivingEntity livingHolder) || !(livingHolder.level() instanceof ServerLevel serverLevel)) {
				return;
			}

			for (var targetSlot : EquipmentSlot.values()) {

				Condition condition = slots().get(targetSlot);

				if (condition == null) {
					continue;
				}

				ItemStack equippedStack = livingHolder.getItemBySlot(targetSlot);
				Context context = this.createContext(livingHolder, equippedStack);

				Context slotContext = context.forChild(".slots").forChild("." + targetSlot.getSerializedName());
				boolean result = condition.test(slotContext);

				if (!slotContext.hasProblems() && !result) {
					InventoryUtil.dropItem(serverLevel, livingHolder, equippedStack.copyAndClear(), false, true, 0);
				}

			}

		}

		public Context createContext(Entity holder, ItemStack stack) {
			return this.createHolderContextBuilder(holder)
				.withRequired(WORN_ITEM, stack)
				.build(holder.level());
		}

		public EnumMap<EquipmentSlot, Condition> slots() {
			return power.slots();
		}

	}

	public static boolean modify(Entity equipper, ItemStack equippedStack, EquipmentSlot targetSlot, BooleanSupplier defaultValue) {

		try (VISITOR) {

			boolean allowed = false;

			for (var instance : Powers.getInstances(equipper, Instance.class, instance -> instance.slots().get(targetSlot) != null)) {

				if (!VISITOR.push(instance)) {
					continue;
				}

				Context instanceContext = instance.createContext(equipper, equippedStack);
				Condition condition = Objects.requireNonNull(instance.slots().get(targetSlot));

				Context slotContext = instanceContext.forChild(".slots").forChild("." + targetSlot.getSerializedName());
				boolean result = condition.test(slotContext);

				if (!slotContext.hasProblems()) {

					if (result) {
						allowed = true;
					}

					else {
						return false;
					}

				}

			}

			return allowed
				|| defaultValue.getAsBoolean();

		}

	}

}
