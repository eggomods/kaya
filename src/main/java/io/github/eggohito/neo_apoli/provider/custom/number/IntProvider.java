package io.github.eggohito.neo_apoli.provider.custom.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ClampedIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ContextIntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliValueProviderKinds;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface IntProvider extends ValueProvider<Integer> {

	Codec<IntProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(IntProvider::getType, Type::mapCodec), ContextIntProvider.INLINE_CODEC, ConstantIntProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, IntProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(IntProvider::getType, Type::streamCodec);

	@Override
	IntProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return NeoApoliValueProviderKinds.INT;
	}

	default int getInt(Context context) {
		return this.getValue(context).orElse(0);
	}

	static Codec<IntProvider> clamped(IntProvider min, IntProvider max) {
		return CODEC.xmap(provider -> new ClampedIntProvider(provider, min, max), Function.identity());
	}

	static Codec<IntProvider> clamped(int min, int max) {
		return clamped(new ConstantIntProvider(min), new ConstantIntProvider(max));
	}

	record Type<P extends IntProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.INT_PROVIDER_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.INT_PROVIDER_TYPE);

	}

}
