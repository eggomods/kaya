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
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.Mth;

public record Rgba(float red, float green, float blue, float alpha) implements Color {

	public static final MapCodec<Rgba> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.floatRange(0.0F, 1.0F).fieldOf("red").forGetter(Rgba::red),
		Codec.floatRange(0.0F, 1.0F).fieldOf("green").forGetter(Rgba::green),
		Codec.floatRange(0.0F, 1.0F).fieldOf("blue").forGetter(Rgba::blue),
		Codec.floatRange(0.0F, 1.0F).fieldOf("alpha").forGetter(Rgba::alpha)
	).apply(instance, Rgba::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Rgba> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, Rgba::red,
		ByteBufCodecs.FLOAT, Rgba::green,
		ByteBufCodecs.FLOAT, Rgba::blue,
		ByteBufCodecs.FLOAT, Rgba::alpha,
		Rgba::new
	);

	public static final Codec<Rgba> STRING_CODEC = ColorRGBA.CODEC.xmap(Rgba::new, Rgba::toColorCode);

	public Rgba {
		red = Mth.clamp(red, 0.0F, 1.0F);
		green = Mth.clamp(green, 0.0F, 1.0F);
		blue = Mth.clamp(blue, 0.0F, 1.0F);
		alpha = Mth.clamp(alpha, 0.0F, 1.0F);
	}

	public Rgba(ColorRGBA colorCode) {
		this(colorCode.rgba());
	}

	public Rgba(int rgba) {
		this(
			getRedFloat(rgba),
			getGreenFloat(rgba),
			getBlueFloat(rgba),
			getAlphaFloat(rgba)
		);
	}

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.RGBA;
	}

	@Override
	public int intValue(Context context) {
		return this.internalGet(); //	No need to use the context in this case, since this class doesn't use any number providers
	}

	private ColorRGBA toColorCode() {
		return new ColorRGBA(this.internalGet());
	}

	private int internalGet() {
		return ARGB.colorFromFloat(alpha(), red(), green(), blue());
	}

	public static int getRed(int rgba) {
		return rgba >>> 24;
	}

	public static float getRedFloat(int rgba) {
		return ARGB.from8BitChannel(getRed(rgba));
	}

	public static int getGreen(int rgba) {
		return rgba >> 16 & 0xFF;
	}

	public static float getGreenFloat(int rgba) {
		return ARGB.from8BitChannel(getGreen(rgba));
	}

	public static int getBlue(int rgba) {
		return rgba >> 8 & 0xFF;
	}

	public static float getBlueFloat(int rgba) {
		return ARGB.from8BitChannel(getBlue(rgba));
	}

	public static int getAlpha(int rgba) {
		return rgba & 0xFF;
	}

	public static float getAlphaFloat(int rgba) {
		return ARGB.from8BitChannel(getAlpha(rgba));
	}

}
