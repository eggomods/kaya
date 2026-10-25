package io.github.eggohito.neo_apoli.provider.custom.command_source;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliCommandSourceProviderTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ConditionalCommandSourceProvider(Condition condition, CommandSourceProvider onTrue, CommandSourceProvider onFalse) implements CommandSourceProvider, ConditionalValueProvider<CommandSourceStack, CommandSourceProvider> {

	public static final MapCodec<ConditionalCommandSourceProvider> CODEC = ConditionalValueProvider.mapCodec(CommandSourceProvider.CODEC, ConditionalCommandSourceProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalCommandSourceProvider> STREAM_CODEC = ConditionalValueProvider.streamCodec(CommandSourceProvider.STREAM_CODEC, ConditionalCommandSourceProvider::new);

	@Override
	public CommandSourceProvider.@NotNull Type<?> getType() {
		return NeoApoliCommandSourceProviderTypes.CONDITIONAL;
	}

}
