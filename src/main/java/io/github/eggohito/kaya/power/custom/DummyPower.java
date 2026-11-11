package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DummyPower(Properties properties) implements Power {

	public static final MapCodec<DummyPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Power.propertiesCodecBuilder(true))
		.apply(instance, DummyPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, DummyPower> STREAM_CODEC = StreamCodec.composite(
		Properties.STREAM_CODEC, DummyPower::properties,
		DummyPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.DUMMY;
	}

	@Override
	public Instance<?> createInstance() {
		return new Instance<>(this) {};
	}

}
