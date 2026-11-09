package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.action.custom.NothingAction;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.parameter.BlockContextParameter;
import io.github.eggohito.kaya.context.parameter.ItemContextParameter;
import io.github.eggohito.kaya.context.parameter.SlotContextParameter;
import io.github.eggohito.kaya.duck.internal.PowerCrafting;
import io.github.eggohito.kaya.mixin.access.CraftingMenuAccessor;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.util.CachedBlock;
import io.github.eggohito.kaya.util.InventoryUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record ModifyRecipeCraftingPower(Optional<ResourceKey<Recipe<?>>> recipe, Optional<ItemStack> replacement, Action onCraftAction, Action onTakeAction, int priority, Properties properties) implements PrioritizedPower<ModifyRecipeCraftingPower> {

	public static final BlockContextParameter CRAFTING_BLOCK = new BlockContextParameter(Kaya.id("crafting_block"));
	public static final SlotContextParameter CRAFTED_ITEM_SLOT = new SlotContextParameter(Kaya.id("crafted_item_slot"));
	public static final ItemContextParameter CRAFTED_ITEM = new ItemContextParameter(Kaya.id("crafted_item"));

	public static final MapCodec<ModifyRecipeCraftingPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceKey.codec(Registries.RECIPE).optionalFieldOf("recipe").forGetter(ModifyRecipeCraftingPower::recipe),
		ItemStack.OPTIONAL_CODEC.optionalFieldOf("replacement").forGetter(ModifyRecipeCraftingPower::replacement),
		Action.CODEC.optionalFieldOf("on_craft_action", NothingAction.INSTANCE).forGetter(ModifyRecipeCraftingPower::onCraftAction),
		Action.CODEC.optionalFieldOf("on_take_action", NothingAction.INSTANCE).forGetter(ModifyRecipeCraftingPower::onTakeAction),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyRecipeCraftingPower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyRecipeCraftingPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyRecipeCraftingPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.RECIPE)), ModifyRecipeCraftingPower::recipe,
		ByteBufCodecs.optional(ItemStack.OPTIONAL_STREAM_CODEC), ModifyRecipeCraftingPower::replacement,
		Action.STREAM_CODEC, ModifyRecipeCraftingPower::onCraftAction,
		Action.STREAM_CODEC, ModifyRecipeCraftingPower::onTakeAction,
		ByteBufCodecs.INT, ModifyRecipeCraftingPower::priority,
		Properties.STREAM_CODEC, ModifyRecipeCraftingPower::properties,
		ModifyRecipeCraftingPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_RECIPE_CRAFTING;
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		onCraftAction().validate(validator.forChild(".on_craft_action"));
		onTakeAction().validate(validator.forChild(".on_take_action"));
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static final class Instance extends Power.Instance<ModifyRecipeCraftingPower> {

		Instance(@NotNull ModifyRecipeCraftingPower power) {
			super(power);
		}

		public Context createContext(Player holder, AbstractContainerMenu menu, SlotAccess assembledItemSlot) {
			return this.createHolderContextBuilder(holder)
				.withOptional(CRAFTING_BLOCK, getCraftingBlock(menu))
				.withRequired(CRAFTED_ITEM_SLOT, assembledItemSlot)
				.withRequired(CRAFTED_ITEM, assembledItemSlot.get())
				.build(holder.level());
		}

		public boolean matches(RecipeHolder<CraftingRecipe> recipeHolder) {
			return power.recipe().isEmpty()
				|| power.recipe().get().equals(recipeHolder.id());
		}

		public void onCrafted(Context context, SlotAccess assembled) {
			power.replacement().ifPresent(assembled::set);
			power.onCraftAction().execute(context.forChild(".on_craft_action"));
		}

		public void onTaken(Context context) {
			power.onTakeAction().execute(context.forChild(".on_take_action"));
		}

	}

	public static ItemStack modifyWhenCrafted(Player crafter, RecipeHolder<CraftingRecipe> recipeHolder, ItemStack assembledItem, AbstractContainerMenu menu, CraftingContainer craftingSlots) {

		if (!(craftingSlots instanceof PowerCrafting powerCrafting)) {
			return assembledItem;
		}

		Map<ModifyRecipeCraftingPower.Instance, Context> modifyingInstances = new Object2ObjectArrayMap<>();
		SlotAccess assembledItemSlot = InventoryUtil.createSingletonSlot(assembledItem);

		for (var instance : new PrioritizedPower.InstanceCollection<>(crafter, Instance.class, instance -> instance.matches(recipeHolder))) {

			Context context = instance.createContext(crafter, menu, assembledItemSlot);

			if (instance.isActive(context)) {
				instance.onCrafted(context, assembledItemSlot);
				modifyingInstances.put(instance, context);
			}

		}

		powerCrafting.kaya$setModifyingInstances(modifyingInstances);
		return assembledItemSlot.get();

	}

	public static ItemStack modifyWhenTaken(Player crafter, ItemStack takenItem, CraftingContainer craftingSlots) {

		if (!(craftingSlots instanceof PowerCrafting powerCrafting)) {
			return takenItem;
		}

		SlotAccess takenItemSlot = InventoryUtil.createSingletonSlot(takenItem);
		var entries = powerCrafting.kaya$getModifyingInstances().entrySet();

		for (var entry : entries) {

			Power.Instance<?> instance = entry.getKey();
			Context context = entry.getValue();

			if (!(instance instanceof Instance actualInstance)) {
				continue;
			}

			actualInstance.onTaken(new Context.Builder(context)
				.withRequired(CRAFTED_ITEM_SLOT, takenItemSlot)
				.withRequired(CRAFTED_ITEM, takenItemSlot.get())
				.build(crafter.level())
			);

		}

		return takenItemSlot.get();

	}

	private static Optional<CachedBlock> getCraftingBlock(AbstractContainerMenu menu) {

		if (menu instanceof CraftingMenu craftingMenu) {
			return ((CraftingMenuAccessor) craftingMenu).getAccess()
				.evaluate(CachedBlock::optionallyFromLoadedPos)
				.flatMap(Function.identity());
		}

		else {
			return Optional.empty();
		}

	}

}
