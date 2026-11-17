package io.github.eggohito.kaya.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.kaya.registry.provider.KayaFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record VectorDotProductFloatProvider(Vec3Provider first, Vec3Provider second) implements FloatProvider {

	public static final MapCodec<VectorDotProductFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("first").forGetter(VectorDotProductFloatProvider::first),
		Vec3Provider.CODEC.fieldOf("second").forGetter(VectorDotProductFloatProvider::second)
	).apply(instance, VectorDotProductFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, VectorDotProductFloatProvider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, VectorDotProductFloatProvider::first,
		Vec3Provider.STREAM_CODEC, VectorDotProductFloatProvider::second,
		VectorDotProductFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.VECTOR_DOT_PRODUCT;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return first().getValue(context.forChild(".first"))
			.flatMap(first -> second().getValue(context.forChild(".second"))
				.map(first::dot)
				.map(Double::floatValue));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		first().validate(validator.forChild(".first"));
		second().validate(validator.forChild(".second"));
	}

}
