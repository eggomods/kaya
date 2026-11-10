package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.color.Color;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ModifyElytraRenderPower(ResourceKey<EquipmentAsset> assetId, Optional<ArmorTrim> trim, Optional<Color> color, int priority, Properties properties) implements PrioritizedPower<ModifyElytraRenderPower> {

	public static final MapCodec<ModifyElytraRenderPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceKey.codec(EquipmentAssets.ROOT_ID).fieldOf("asset_id").forGetter(ModifyElytraRenderPower::assetId),
		ArmorTrim.CODEC.optionalFieldOf("trim").forGetter(ModifyElytraRenderPower::trim),
		Color.CODEC.optionalFieldOf("color").forGetter(ModifyElytraRenderPower::color),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyElytraRenderPower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyElytraRenderPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyElytraRenderPower> STREAM_CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(EquipmentAssets.ROOT_ID), ModifyElytraRenderPower::assetId,
		ByteBufCodecs.optional(ArmorTrim.STREAM_CODEC), ModifyElytraRenderPower::trim,
		ByteBufCodecs.optional(Color.STREAM_CODEC), ModifyElytraRenderPower::color,
		ByteBufCodecs.INT, ModifyElytraRenderPower::priority,
		Properties.STREAM_CODEC, ModifyElytraRenderPower::properties,
		ModifyElytraRenderPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_ELYTRA_RENDER;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<ModifyElytraRenderPower> {

		protected Instance(@NotNull ModifyElytraRenderPower power) {
			super(power);
		}

		public ResourceKey<EquipmentAsset> assetId() {
			return power.assetId();
		}

		public Optional<ArmorTrim> trim() {
			return power.trim();
		}

		public Optional<DyedItemColor> colorAsDye(Context context) {
			return power.color()
				.map(color -> color.intValue(context.forChild(".color")))
				.map(DyedItemColor::new);
		}

		public boolean isActive(Entity holder) {
			return this.isActive(this.createHolderContext(holder));
		}

	}

}
