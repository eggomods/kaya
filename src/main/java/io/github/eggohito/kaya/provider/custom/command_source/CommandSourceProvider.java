package io.github.eggohito.kaya.provider.custom.command_source;

import com.mojang.serialization.Codec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.provider.ValueProvider;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.registry.provider.KayaValueProviderKinds;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface CommandSourceProvider extends ValueProvider<CommandSourceStack> {

	Codec<CommandSourceProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(CommandSourceProvider::getType, Type::mapCodec), SimpleCommandSourceProvider.CODEC));

	StreamCodec<RegistryFriendlyByteBuf, CommandSourceProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(CommandSourceProvider::getType, Type::streamCodec);

	@Override
	CommandSourceProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return KayaValueProviderKinds.COMMAND_SOURCE;
	}

	interface Type<P extends CommandSourceProvider> extends ValueProvider.Type<P> {

		FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.COMMAND_SOURCE_PROVIDER_TYPE, Kaya.GLOBAL_ALIASES);

		Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
