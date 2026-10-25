package io.github.eggohito.neo_apoli.provider.custom.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEffectProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConstantEffectProvider(MobEffectInstance value) implements EffectProvider {

	public static final MapCodec<ConstantEffectProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(MobEffectInstance.CODEC.fieldOf("value").forGetter(ConstantEffectProvider::value))
		.apply(instance, ConstantEffectProvider::new)
	);

	public static final Codec<ConstantEffectProvider> INLINE_CODEC = MobEffectInstance.CODEC.xmap(
		ConstantEffectProvider::new,
		ConstantEffectProvider::value
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantEffectProvider> STREAM_CODEC = StreamCodec.composite(
		MobEffectInstance.STREAM_CODEC, ConstantEffectProvider::value,
		ConstantEffectProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliEffectProviderTypes.CONSTANT;
	}

	@Override
	public Optional<MobEffectInstance> getValue(Context context) {
		return Optional.of(value());
	}

}
