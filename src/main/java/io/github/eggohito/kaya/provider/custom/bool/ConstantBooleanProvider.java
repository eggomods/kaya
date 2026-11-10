package io.github.eggohito.kaya.provider.custom.bool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.registry.provider.KayaBooleanProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConstantBooleanProvider(boolean value) implements BooleanProvider {

	public static final ConstantBooleanProvider TRUE = new ConstantBooleanProvider(true);
	public static final ConstantBooleanProvider FALSE = new ConstantBooleanProvider(false);

	public static final MapCodec<ConstantBooleanProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Codec.BOOL.fieldOf("value").forGetter(ConstantBooleanProvider::value))
		.apply(instance, ConstantBooleanProvider::new)
	);

	public static final Codec<ConstantBooleanProvider> INLINE_CODEC = Codec.BOOL.xmap(
		ConstantBooleanProvider::new,
		ConstantBooleanProvider::value
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantBooleanProvider> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, ConstantBooleanProvider::value,
		ConstantBooleanProvider::new
	);

	@Override
	public @NotNull BooleanProvider.Type<?> getType() {
		return KayaBooleanProviderTypes.CONSTANT;
	}

	@Override
	public Optional<Boolean> getValue(Context context) {
		return Optional.of(value());
	}

}
