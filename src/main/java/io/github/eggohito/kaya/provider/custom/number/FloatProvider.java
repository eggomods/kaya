package io.github.eggohito.kaya.provider.custom.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.provider.ValueProvider;
import io.github.eggohito.kaya.provider.custom.number.floats.ClampedFloatProvider;
import io.github.eggohito.kaya.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.kaya.provider.custom.number.floats.ContextFloatProvider;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.registry.provider.KayaValueProviderKinds;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface FloatProvider extends ValueProvider<Float> {

	Codec<FloatProvider> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(FloatProvider::getType, Type::mapCodec), ContextFloatProvider.INLINE_CODEC, ConstantFloatProvider.INLINE_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, FloatProvider> STREAM_CODEC = Type.STREAM_CODEC.dispatch(FloatProvider::getType, Type::streamCodec);

	@Override
	FloatProvider.@NotNull Type<?> getType();

	@Override
	default @NotNull Kind<?> getKind() {
		return KayaValueProviderKinds.FLOAT;
	}

	default float getFloat(Context context) {
		return this.getValue(context).orElse(0.0F);
	}

	static Codec<FloatProvider> clamped(FloatProvider min, FloatProvider max) {
		return CODEC.xmap(provider -> new ClampedFloatProvider(provider, min, max), Function.identity());
	}

	static Codec<FloatProvider> clamped(float min, float max) {
		return clamped(new ConstantFloatProvider(min), new ConstantFloatProvider(max));
	}

	record Type<P extends FloatProvider>(MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) implements ValueProvider.Type<P> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.FLOAT_PROVIDER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
