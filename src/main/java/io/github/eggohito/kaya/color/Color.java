package io.github.eggohito.kaya.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.color.custom.Rgba;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;

/**
 * 	Color is an interface used to store ARGB data in object form. Getting the ARGB data as a primitive integer also
 * 	has access to a {@link Context}, which allows for further capabilities (e.g: using context-based objects for
 * 	generating ARGB values)
 */
public interface Color extends ContextUser {

	Codec<Color> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(Color::getType, Type::mapCodec), Rgba.STRING_CODEC));

	StreamCodec<RegistryFriendlyByteBuf, Color> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Color::getType, Type::streamCodec);

	Type<?> getType();

	int intValue(Context context);

	//	TODO: Add an parameter for determining how the ARGB colors are mixed
	static int mix(int first, int second) {
		return ARGB.multiply(first, second);
	}

	record Type<C extends Color>(MapCodec<C> mapCodec, StreamCodec<RegistryFriendlyByteBuf, C> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.COLOR_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
