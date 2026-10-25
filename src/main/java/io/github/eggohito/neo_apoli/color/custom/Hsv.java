package io.github.eggohito.neo_apoli.color.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public record Hsv(float hue, float saturation, float value, float alpha) implements Color {

	public static final MapCodec<Hsv> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.floatRange(0.0F, 1.0F).fieldOf("hue").forGetter(Hsv::hue),
		Codec.floatRange(0.0F, 1.0F).fieldOf("saturation").forGetter(Hsv::saturation),
		Codec.floatRange(0.0F, 1.0F).fieldOf("value").forGetter(Hsv::value),
		Codec.floatRange(0.0F, 1.0F).fieldOf("alpha").forGetter(Hsv::alpha)
	).apply(instance, Hsv::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Hsv> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, Hsv::hue,
		ByteBufCodecs.FLOAT, Hsv::saturation,
		ByteBufCodecs.FLOAT, Hsv::value,
		ByteBufCodecs.FLOAT, Hsv::alpha,
		Hsv::new
	);

	public Hsv {
		hue = Mth.clamp(hue, 0.0F, 1.0F);
		saturation = Mth.clamp(saturation, 0.0F, 1.0F);
		value = Mth.clamp(value, 0.0F, 1.0F);
		alpha = Mth.clamp(alpha, 0.0F, 1.0F);
	}

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.HSV;
	}

	@Override
	public int intValue(Context context) {
		return Mth.hsvToArgb(hue(), saturation(), value(), ARGB.as8BitChannel(alpha()));
	}

}
