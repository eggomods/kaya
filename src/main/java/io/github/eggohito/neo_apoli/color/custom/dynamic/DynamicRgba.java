package io.github.eggohito.neo_apoli.color.custom.dynamic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ClampedFloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;

public record DynamicRgba(FloatProvider red, FloatProvider green, FloatProvider blue, FloatProvider alpha) implements Color {

	public static final MapCodec<DynamicRgba> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("red").forGetter(DynamicRgba::red),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("green").forGetter(DynamicRgba::green),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("blue").forGetter(DynamicRgba::blue),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("alpha").forGetter(DynamicRgba::alpha)
	).apply(instance, DynamicRgba::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DynamicRgba> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, DynamicRgba::red,
		FloatProvider.STREAM_CODEC, DynamicRgba::green,
		FloatProvider.STREAM_CODEC, DynamicRgba::blue,
		FloatProvider.STREAM_CODEC, DynamicRgba::alpha,
		DynamicRgba::new
	);

	public DynamicRgba {

		if (!(red instanceof ClampedFloatProvider)) {
			red = new ClampedFloatProvider(red, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

		if (!(green instanceof ClampedFloatProvider)) {
			green = new ClampedFloatProvider(green, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

		if (!(blue instanceof ClampedFloatProvider)) {
			blue = new ClampedFloatProvider(blue, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

		if (!(alpha instanceof ClampedFloatProvider)) {
			alpha = new ClampedFloatProvider(alpha, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

	}

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.DYNAMIC_RGBA;
	}

	@Override
	public void validate(ContextValidator validator) {
		Color.super.validate(validator);
		red().validate(validator.forChild(".red"));
		green().validate(validator.forChild(".green"));
		blue().validate(validator.forChild(".blue"));
		alpha().validate(validator.forChild(".alpha"));
	}

	@Override
	public int intValue(Context context) {
		return ARGB.colorFromFloat(alpha(context), red(context), green(context), blue(context));
	}

	public float red(Context context) {
		return red().getValue(context.forChild(".red")).orElse(1.0F);
	}

	public float green(Context context) {
		return green().getValue(context.forChild(".green")).orElse(1.0F);
	}

	public float blue(Context context) {
		return blue().getValue(context.forChild(".blue")).orElse(1.0F);
	}

	public float alpha(Context context) {
		return alpha().getValue(context.forChild(".alpha")).orElse(1.0F);
	}

}
