package io.github.eggohito.neo_apoli.provider.custom.damage_source;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDamageSourceProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalDamageSourceProvider(List<Entry<DamageSourceProvider>> entries, DamageSourceProvider defaultValue) implements DamageSourceProvider, CompositeConditionalValueProvider<DamageSource, DamageSourceProvider> {

	public static final MapCodec<CompositeConditionalDamageSourceProvider> CODEC = CompositeConditionalValueProvider.mapCodec(DamageSourceProvider.CODEC, CompositeConditionalDamageSourceProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalDamageSourceProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(DamageSourceProvider.STREAM_CODEC, CompositeConditionalDamageSourceProvider::new);

	@Override
	public DamageSourceProvider.@NotNull Type<?> getType() {
		return NeoApoliDamageSourceProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
