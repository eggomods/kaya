package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ModifyModelShakingPower(Properties properties) implements Power {

	public static final MapCodec<ModifyModelShakingPower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addPropertiesField(instance, true)
		.apply(instance, ModifyModelShakingPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyModelShakingPower> STREAM_CODEC = StreamCodec.composite(
		Properties.STREAM_CODEC, ModifyModelShakingPower::properties,
		ModifyModelShakingPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_MODEL_SHAKING;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<ModifyModelShakingPower> {

		protected Instance(@NotNull ModifyModelShakingPower power) {
			super(power);
		}

		public boolean isActive(Entity holder) {
			return this.isActive(this.createHolderContext(holder));
		}

	}

}
