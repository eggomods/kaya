package io.github.eggohito.neo_apoli.provider.custom.command_source;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliValueProviderKinds;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface CommandSourceProvider extends ValueProvider<CommandSourceStack> {

	Codec<CommandSourceProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(CommandSourceProvider::getType, Type::mapCodec), SimpleCommandSourceProvider.CODEC));

	StreamCodec<RegistryFriendlyByteBuf, CommandSourceProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(CommandSourceProvider::getType, Type::streamCodec);

	@Override
	CommandSourceProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return NeoApoliValueProviderKinds.COMMAND_SOURCE;
	}

	interface Type<P extends CommandSourceProvider> extends ValueProvider.Type<P> {

		FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.COMMAND_SOURCE_PROVIDER_TYPE);

		Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.COMMAND_SOURCE_PROVIDER_TYPE);

	}

}
