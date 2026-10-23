package io.github.eggohito.neo_apoli.provider.custom.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ClampedFloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ContextFloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliValueProviderKinds;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
		return NeoApoliValueProviderKinds.FLOAT;
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

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.FLOAT_PROVIDER_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.FLOAT_PROVIDER_TYPE);

	}

}
