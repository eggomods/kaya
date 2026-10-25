package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDirectionProviderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ConditionalDirectionProvider(Condition condition, DirectionProvider onTrue, DirectionProvider onFalse) implements DirectionProvider, ConditionalValueProvider<Direction, DirectionProvider> {

	public static final MapCodec<ConditionalDirectionProvider> CODEC = ConditionalValueProvider.mapCodec(DirectionProvider.CODEC, ConditionalDirectionProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalDirectionProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(DirectionProvider.STREAM_CODEC, ConditionalDirectionProvider::new);

	@Override
	public DirectionProvider.@NotNull Type<?> getType() {
		return NeoApoliDirectionProviderTypes.CONDITIONAL;
	}

}
