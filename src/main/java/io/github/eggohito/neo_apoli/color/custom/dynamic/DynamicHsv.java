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
import net.minecraft.util.Mth;

public record DynamicHsv(FloatProvider hue, FloatProvider saturation, FloatProvider value, FloatProvider alpha) implements Color {

	public static final MapCodec<DynamicHsv> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("hue").forGetter(DynamicHsv::hue),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("saturation").forGetter(DynamicHsv::saturation),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("value").forGetter(DynamicHsv::value),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("alpha").forGetter(DynamicHsv::alpha)
	).apply(instance, DynamicHsv::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DynamicHsv> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, DynamicHsv::hue,
		FloatProvider.STREAM_CODEC, DynamicHsv::saturation,
		FloatProvider.STREAM_CODEC, DynamicHsv::value,
		FloatProvider.STREAM_CODEC, DynamicHsv::alpha,
		DynamicHsv::new
	);

	public DynamicHsv {

		if (!(hue instanceof ClampedFloatProvider)) {
			hue = new ClampedFloatProvider(hue, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

		if (!(saturation instanceof ClampedFloatProvider)) {
			saturation = new ClampedFloatProvider(saturation, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

		if (!(value instanceof ClampedFloatProvider)) {
			value = new ClampedFloatProvider(value, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

		if (!(alpha instanceof ClampedFloatProvider)) {
			alpha = new ClampedFloatProvider(alpha, new ConstantFloatProvider(0.0F), new ConstantFloatProvider(1.0F));
		}

	}

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.DYNAMIC_HSV;
	}

	@Override
	public int intValue(Context context) {
		return Mth.hsvToArgb(hue(context), saturation(context), value(context), ARGB.as8BitChannel(alpha(context)));
	}

	@Override
	public void validate(ContextValidator validator) {
		Color.super.validate(validator);
		hue().validate(validator.forChild(".hue"));
		saturation().validate(validator.forChild(".saturation"));
		value().validate(validator.forChild(".value"));
		alpha().validate(validator.forChild(".alpha"));
	}

	public float hue(Context context) {
		return hue().getValue(context.forChild(".hue")).orElse(1.0F);
	}

	public float saturation(Context context) {
		return saturation().getValue(context.forChild(".saturation")).orElse(1.0F);
	}

	public float value(Context context) {
		return value().getValue(context.forChild(".value")).orElse(1.0F);
	}

	public float alpha(Context context) {
		return alpha().getValue(context.forChild(".alpha")).orElse(1.0F);
	}

}
