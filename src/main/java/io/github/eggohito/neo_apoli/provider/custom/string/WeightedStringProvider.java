package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.NotNull;

public record WeightedStringProvider(WeightedList<StringProvider> entries) implements StringProvider, WeightedValueProvider<String, StringProvider> {

	public static final MapCodec<WeightedStringProvider> CODEC = WeightedValueProvider.mapCodec(StringProvider.CODEC, WeightedStringProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedStringProvider> STREAM_CODEC = WeightedValueProvider.streamCodec(StringProvider.STREAM_CODEC, WeightedStringProvider::new);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.WEIGHTED;
	}

}
