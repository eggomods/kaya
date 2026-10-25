package io.github.eggohito.neo_apoli.provider.custom.entity;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEntityProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record WeightedEntityProvider(WeightedList<EntityProvider> entries) implements EntityProvider, WeightedValueProvider<Entity, EntityProvider> {

	public static final MapCodec<WeightedEntityProvider> CODEC = WeightedValueProvider.mapCodec(EntityProvider.CODEC, WeightedEntityProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedEntityProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(EntityProvider.STREAM_CODEC, WeightedEntityProvider::new);

	@Override
	public EntityProvider.@NotNull Type<?> getType() {
		return NeoApoliEntityProviderTypes.WEIGHTED;
	}

}
