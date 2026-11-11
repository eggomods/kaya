package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.codec.KayaMapCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public record CraftingRecipePower(RecipeHolder<CraftingRecipe> recipe, int priority, Properties properties) implements PrioritizedPower<CraftingRecipePower> {

	public static final MapCodec<CraftingRecipePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KayaMapCodecs.CRAFTING_RECIPE_ENTRY.fieldOf("recipe").forGetter(CraftingRecipePower::recipe),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(CraftingRecipePower::priority),
		Power.propertiesCodecBuilder(false)
	).apply(instance, CraftingRecipePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CraftingRecipePower> STREAM_CODEC = StreamCodec.composite(
		KayaStreamCodecs.CRAFTING_RECIPE_ENTRY, CraftingRecipePower::recipe,
		ByteBufCodecs.INT, CraftingRecipePower::priority,
		Properties.STREAM_CODEC, CraftingRecipePower::properties,
		CraftingRecipePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CRAFTING_RECIPE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<CraftingRecipePower> {

		protected Instance(@NotNull CraftingRecipePower power) {
			super(power);
		}

		public RecipeHolder<CraftingRecipe> recipe() {
			return power.recipe();
		}

	}

}
