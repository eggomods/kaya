package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalStringProvider(List<CompositeConditional.Entry<StringProvider>> entries, StringProvider defaultValue) implements StringProvider, CompositeConditionalValueProvider<String, StringProvider> {

	public static final MapCodec<CompositeConditionalStringProvider> CODEC = CompositeConditionalValueProvider.mapCodec(StringProvider.CODEC, CompositeConditionalStringProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalStringProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(StringProvider.STREAM_CODEC, CompositeConditionalStringProvider::new);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
