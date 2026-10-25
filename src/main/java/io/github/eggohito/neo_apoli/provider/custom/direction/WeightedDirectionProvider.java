package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDirectionProviderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

public record WeightedDirectionProvider(WeightedList<DirectionProvider> entries) implements DirectionProvider, WeightedValueProvider<Direction, DirectionProvider> {

	public static final MapCodec<WeightedDirectionProvider> CODEC = WeightedValueProvider.mapCodec(DirectionProvider.CODEC, WeightedDirectionProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedDirectionProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(DirectionProvider.STREAM_CODEC, WeightedDirectionProvider::new);

	@Override
	public DirectionProvider.@NotNull Type<?> getType() {
		return NeoApoliDirectionProviderTypes.WEIGHTED;
	}

}
