package io.github.eggohito.neo_apoli.provider.custom.damage_source;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDamageSourceProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

public record ConditionalDamageSourceProvider(Condition condition, DamageSourceProvider onTrue, DamageSourceProvider onFalse) implements DamageSourceProvider, ConditionalValueProvider<DamageSource, DamageSourceProvider> {

	public static final MapCodec<ConditionalDamageSourceProvider> CODEC = ConditionalValueProvider.mapCodec(DamageSourceProvider.CODEC, ConditionalDamageSourceProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalDamageSourceProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(DamageSourceProvider.STREAM_CODEC, ConditionalDamageSourceProvider::new);

	@Override
	public DamageSourceProvider.@NotNull Type<?> getType() {
		return NeoApoliDamageSourceProviderTypes.CONDITIONAL;
	}

}
