package io.github.eggohito.neo_apoli.provider.custom.entity;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEntityProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ConditionalEntityProvider(Condition condition, EntityProvider onTrue, EntityProvider onFalse) implements EntityProvider, ConditionalValueProvider<Entity, EntityProvider> {

	public static final MapCodec<ConditionalEntityProvider> CODEC = ConditionalValueProvider.mapCodec(EntityProvider.CODEC, ConditionalEntityProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalEntityProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(EntityProvider.STREAM_CODEC, ConditionalEntityProvider::new);

	@Override
	public EntityProvider.@NotNull Type<?> getType() {
		return NeoApoliEntityProviderTypes.CONDITIONAL;
	}

}
