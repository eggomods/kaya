package io.github.eggohito.kaya.mixin.impl.power.custom.crafting_recipe;

import io.github.eggohito.kaya.duck.internal.PowerRecipeDisplayHolder;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundPowerRecipeDisplaysUpdatePacket;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.power.custom.CraftingRecipePower;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.recipe.PowerCraftingRecipe;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements PowerRecipeDisplayHolder {

	@Shadow
	private RecipeMap recipes;

	@Shadow
	private Map<ResourceKey<Recipe<?>>, List<RecipeManager.ServerDisplayInfo>> recipeToDisplay;

	@Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
	void onFinalizeLoad(FeatureFlagSet features, CallbackInfo ci) {

		ObjectCollection<RecipeHolder<?>> recipeEntries = new ObjectOpenHashSet<>(this.recipes.values());
		Object2IntMap<ResourceKey<Recipe<?>>> replacedRecipes = Util.make(new Object2IntOpenHashMap<>(), map -> recipeEntries.forEach(recipeEntry -> map.put(recipeEntry.id(), 0)));

		for (PowerHolder<?> powerHolder : PowerManager.getInstance().values()) {

			if (!(powerHolder.value() instanceof CraftingRecipePower craftingRecipePower)) {
				continue;
			}

			RecipeHolder<CraftingRecipe> recipeHolder = craftingRecipePower.recipe();
			int priority = craftingRecipePower.priority();

			ResourceKey<Recipe<?>> recipeKey = recipeHolder.id();
			CraftingRecipe recipe = recipeHolder.value();

			if (!replacedRecipes.containsKey(recipeKey) || priority > replacedRecipes.getInt(recipeKey)) {

				var replacement = new RecipeHolder<>(recipeKey, new PowerCraftingRecipe(powerHolder.id(), recipe));

				recipeEntries.remove(recipeHolder);
				recipeEntries.add(replacement);

			}

			replacedRecipes.put(recipeKey, priority);

		}

		this.recipes = RecipeMap.create(recipeEntries);

	}

	@Unique
	private final Int2ObjectMap<PowerIdentifier> kaya$powerIdsByIndex = new Int2ObjectOpenHashMap<>();

	@Inject(method = "finalizeRecipeLoading", at = @At("TAIL"))
	void afterFinalizeLoad(FeatureFlagSet features, CallbackInfo ci) {

		this.kaya$powerIdsByIndex.clear();
		this.recipeToDisplay.forEach((key, serverRecipes) -> serverRecipes.forEach(serverRecipe -> {

			if (serverRecipe.parent().value() instanceof PowerCraftingRecipe(PowerIdentifier id, CraftingRecipe ignored)) {
				kaya$powerIdsByIndex.put(serverRecipe.display().id().index(), id);
			}

		}));

	}

	@Override
	public Int2ObjectMap<PowerIdentifier> kaya$getPowerIdsByIndex() {
		return new Int2ObjectOpenHashMap<>(this.kaya$powerIdsByIndex);
	}

	@Override
	public void kaya$sendAll(ServerPlayer recipient) {

		ClientboundPowerRecipeDisplaysUpdatePacket packet = new ClientboundPowerRecipeDisplaysUpdatePacket(this.kaya$getPowerIdsByIndex());

		if (ServerPlayNetworking.canSend(recipient, packet.type())) {
			ServerPlayNetworking.send(recipient, packet);
		}

	}

}
