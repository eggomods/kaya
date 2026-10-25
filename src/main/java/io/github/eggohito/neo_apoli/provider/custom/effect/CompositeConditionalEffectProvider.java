package io.github.eggohito.neo_apoli.provider.custom.effect;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEffectProviderTypes;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalEffectProvider(List<CompositeConditional.Entry<EffectProvider>> entries, EffectProvider defaultValue) implements EffectProvider, CompositeConditionalValueProvider<MobEffectInstance, EffectProvider> {

	public static final MapCodec<CompositeConditionalEffectProvider> CODEC = CompositeConditionalValueProvider.mapCodec(EffectProvider.CODEC, CompositeConditionalEffectProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalEffectProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(EffectProvider.STREAM_CODEC, CompositeConditionalEffectProvider::new);

	@Override
	public EffectProvider.@NotNull Type<?> getType() {
		return NeoApoliEffectProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
