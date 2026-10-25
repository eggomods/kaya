package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ConstantNbtProvider(Tag value) implements NbtProvider {

	public static final MapCodec<ConstantNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.TAG.fieldOf("value").forGetter(ConstantNbtProvider::value)
	).apply(instance, ConstantNbtProvider::new));

	public static final Codec<ConstantNbtProvider> INLINE_CODEC = NeoApoliCodecs.TAG.xmap(
		ConstantNbtProvider::new,
		ConstantNbtProvider::value
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantNbtProvider> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.TRUSTED_TAG, ConstantNbtProvider::value,
		ConstantNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.CONSTANT;
	}

	@Override
	public Optional<Tag> getValue(Context context) {
		return Optional.of(this.value());
	}

}
