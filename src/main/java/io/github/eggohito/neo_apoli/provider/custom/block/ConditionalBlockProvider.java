package io.github.eggohito.neo_apoli.provider.custom.block;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBlockProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ConditionalBlockProvider(Condition condition, BlockProvider onTrue, BlockProvider onFalse) implements BlockProvider, ConditionalValueProvider<CachedBlock, BlockProvider> {

	public static final MapCodec<ConditionalBlockProvider> CODEC = ConditionalValueProvider.mapCodec(BlockProvider.CODEC, ConditionalBlockProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalBlockProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(BlockProvider.STREAM_CODEC, ConditionalBlockProvider::new);

	@Override
	public BlockProvider.@NotNull Type<?> getType() {
		return NeoApoliBlockProviderTypes.CONDITIONAL;
	}

}
