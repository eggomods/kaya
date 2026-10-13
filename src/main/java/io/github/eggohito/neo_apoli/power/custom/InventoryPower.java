package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.container_menu.ContainerMenu;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.key.KeyReference;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliContainerMenuTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.InventoryUtil;
import io.github.eggohito.neo_apoli.util.StackInContainer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public record InventoryPower(Optional<Condition> activeCondition, Component title, ContainerMenu menu, Condition dropOnDeathCondition, BooleanProvider recoverable, KeyReference key, int priority) implements PrioritizedPower<InventoryPower> {

	public static final MapCodec<InventoryPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(ComponentSerialization.CODEC.optionalFieldOf("title", Component.translatable("container.inventory")).forGetter(InventoryPower::title))
		.and(ContainerMenu.CODEC.optionalFieldOf("menu", NeoApoliContainerMenuTypes.GENERIC_3X3).forGetter(InventoryPower::menu))
		.and(Condition.CODEC.optionalFieldOf("drop_on_death_condition", new ConstantCondition(false)).forGetter(InventoryPower::dropOnDeathCondition))
		.and(BooleanProvider.CODEC.optionalFieldOf("recoverable", new ConstantBooleanProvider(true)).forGetter(InventoryPower::recoverable))
		.and(KeyReference.CODEC.fieldOf("key").forGetter(InventoryPower::key))
		.and(Codec.INT.optionalFieldOf("priority", 0).forGetter(InventoryPower::priority))
		.apply(instance, InventoryPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, InventoryPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		ComponentSerialization.TRUSTED_STREAM_CODEC, InventoryPower::title,
		ContainerMenu.STREAM_CODEC, InventoryPower::menu,
		Condition.STREAM_CODEC, InventoryPower::dropOnDeathCondition,
		BooleanProvider.STREAM_CODEC, InventoryPower::recoverable,
		KeyReference.STREAM_CODEC, InventoryPower::key,
		ByteBufCodecs.INT, InventoryPower::priority,
		InventoryPower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.INVENTORY;
	}

	@Override
	public Instance createInstance() {
		return new Instance(this);
	}

	@Override
	public int priority() {
		return priority;
	}

	public static class Instance extends Power.Instance<InventoryPower> implements Container {

		private final ContainerMenu menu;
		private final SimpleContainer container;

		private final MutableBoolean changed;

		protected Instance(@NotNull InventoryPower power) {
			super(power);
			this.menu = power.menu();
			this.container = new SimpleContainer(this.menu.size());
			this.changed = new MutableBoolean(true);
			this.container.addListener(ignored -> changed.setTrue());
		}

		@Override
		public void onRevoked(Entity holder) {

			Context context = this.createHolderContext(holder);

			if (power.recoverable().getBoolean(context.forChild(".recoverable"))) {
				this.dropItemsOnLost(holder);
			}

		}

		@Override
		public void onTick(Entity holder) {

			if (!holder.level().isClientSide()) {
				this.syncData(holder);
			}

			changed.setFalse();

		}

		@Override
		public boolean shouldTick(Entity holder) {
			return changed.isTrue();
		}

		@Override
		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {

			DataResult<Unit> identity = DataResult.success(Unit.INSTANCE);
			I itemsInput = mapInput.get("items");

			if (itemsInput == null) {
				return identity;
			}

			DataResult<List<StackInContainer>> itemsResult = StackInContainer.LIST_CODEC.parse(ops, itemsInput);
			identity = identity.apply2stable((unit, ignoredItems) -> unit, itemsResult);

			if (!itemsResult.hasResultOrPartial()) {
				return identity;
			}

			List<StackInContainer> stacksInContainer = itemsResult.resultOrPartial().orElseThrow();
			this.container.getItems().clear();

			for (var stackInContainer : stacksInContainer) {

				ItemStack stack = stackInContainer.stack();
				container.getItems().set(stackInContainer.slot(), stack);

				stack.limitSize(this.getMaxStackSize(stack));

			}

			return DataResult.success(Unit.INSTANCE);

		}

		@Override
		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {

			List<StackInContainer> converted = new ObjectArrayList<>();
			ListIterator<ItemStack> iterator = container.getItems().listIterator();

			while (iterator.hasNext()) {

				int index = iterator.nextIndex();
				ItemStack stack = iterator.next();

				if (!stack.isEmpty()) {
					converted.add(new StackInContainer(stack, index));
				}

			}

			return prefix.add("items", StackInContainer.CODEC.listOf().encodeStart(ops, converted));

		}

		@Override
		public int getContainerSize() {
			return container.getContainerSize();
		}

		@Override
		public boolean isEmpty() {
			return container.isEmpty();
		}

		@Override
		public @NotNull ItemStack getItem(int slot) {
			return container.getItem(slot);
		}

		@Override
		public @NotNull ItemStack removeItem(int slot, int amount) {
			return container.removeItem(slot, amount);
		}

		@Override
		public @NotNull ItemStack removeItemNoUpdate(int slot) {
			return container.removeItemNoUpdate(slot);
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			container.setItem(slot, stack);
		}

		@Override
		public void setChanged() {
			container.setChanged();
		}

		@Override
		public boolean stillValid(Player player) {
			return container.stillValid(player);
		}

		@Override
		public void clearContent() {
			container.clearContent();
		}

		public boolean shouldOpen(Context context, KeyState previous, KeyState current) {
			return previous.equals(current)
				&& current.pressed()
				&& this.isActive(context)
				&& power.key().continuouslyPressed(context, previous, current);
		}

		public boolean open(Player holder) {
			return holder.openMenu(new SimpleMenuProvider(menu.constructor(this), power.title())).isPresent();
		}

		public boolean shouldDropOnDeath(Context context) {
			return this.isActive(context)
				&& power.dropOnDeathCondition().test(context.forChild(".drop_on_death_condition"));
		}

		public void dropItemsOnLost(Entity holder) {

			if (!(holder.level() instanceof ServerLevel serverLevel)) {
				return;
			}

			for (int i = 0; i < this.getContainerSize(); i++) {

				ItemStack droppedItem = this.removeItemNoUpdate(i);

				if (holder instanceof Player player) {
					player.getInventory().placeItemBackInInventory(droppedItem);
				}

				else {
					InventoryUtil.dropItem(serverLevel, holder, droppedItem, true, true, 0);
				}

			}

		}

		public void dropItemsOnDeath(Entity holder) {

			if (!(holder.level() instanceof ServerLevel serverLevel)) {
				return;
			}

			for (int index = 0; index < this.getContainerSize(); index++) {

				Context itemContext = this.createHolderContextBuilder(holder)
					.withRequired(NeoApoliContextParameters.ITEM_IN_CONTAINER, this.getItem(index))
					.build(serverLevel);

				if (this.shouldDropOnDeath(itemContext)) {
					InventoryUtil.dropItem(serverLevel, holder, this.removeItemNoUpdate(index), true, true);
				}

			}

		}

	}

}
