package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ConditionalStringProvider(Condition condition, StringProvider onTrue, StringProvider onFalse) implements StringProvider, ConditionalValueProvider<String, StringProvider> {

	public static final MapCodec<ConditionalStringProvider> CODEC = ConditionalValueProvider.mapCodec(StringProvider.CODEC, ConditionalStringProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalStringProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(StringProvider.STREAM_CODEC, ConditionalStringProvider::new);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.CONDITIONAL;
	}

}
