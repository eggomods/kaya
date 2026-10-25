package io.github.eggohito.neo_apoli.provider.custom.damage_source;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDamageSourceProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

public record WeightedDamageSourceProvider(WeightedList<DamageSourceProvider> entries) implements DamageSourceProvider, WeightedValueProvider<DamageSource, DamageSourceProvider> {

	public static final MapCodec<WeightedDamageSourceProvider> CODEC = WeightedValueProvider.mapCodec(DamageSourceProvider.CODEC, WeightedDamageSourceProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedDamageSourceProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(DamageSourceProvider.STREAM_CODEC, WeightedDamageSourceProvider::new);

	@Override
	public DamageSourceProvider.@NotNull Type<?> getType() {
		return NeoApoliDamageSourceProviderTypes.WEIGHTED;
	}

}
