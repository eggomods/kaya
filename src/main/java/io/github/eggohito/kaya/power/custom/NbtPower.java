package io.github.eggohito.kaya.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.codec.KayaMapCodecs;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record NbtPower(Properties properties) implements Power {

	public static final MapCodec<NbtPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Power.propertiesCodecBuilder(false))
		.apply(instance, NbtPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, NbtPower> STREAM_CODEC = StreamCodec.composite(
		Properties.STREAM_CODEC, NbtPower::properties,
		NbtPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.NBT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<NbtPower> {

		protected CompoundTag data = new CompoundTag();

		protected Instance(@NotNull NbtPower power) {
			super(power);
		}

		@Override
		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {
			return KayaMapCodecs.COMPOUND_TAG.decode(ops, mapInput)
				.map(compoundTag -> this.data = compoundTag)
				.map(compoundTag -> Unit.INSTANCE);
		}

		@Override
		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {
			return KayaMapCodecs.COMPOUND_TAG.encode(this.data, ops, prefix);
		}

		@Override
		public boolean isImmutable(Entity holder) {
			return false;
		}

	}

}
