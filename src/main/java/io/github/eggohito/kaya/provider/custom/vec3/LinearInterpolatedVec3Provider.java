package io.github.eggohito.kaya.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.registry.provider.KayaVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record LinearInterpolatedVec3Provider(FloatProvider delta, Vec3Provider start, Vec3Provider end) implements Vec3Provider {

	public static final MapCodec<LinearInterpolatedVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("delta").forGetter(LinearInterpolatedVec3Provider::delta),
		Vec3Provider.CODEC.fieldOf("start").forGetter(LinearInterpolatedVec3Provider::start),
		Vec3Provider.CODEC.fieldOf("end").forGetter(LinearInterpolatedVec3Provider::end)
	).apply(instance, LinearInterpolatedVec3Provider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LinearInterpolatedVec3Provider> STREAM_CODEC = StreamCodec.composite(
		FloatProvider.STREAM_CODEC, LinearInterpolatedVec3Provider::delta,
		Vec3Provider.STREAM_CODEC, LinearInterpolatedVec3Provider::start,
		Vec3Provider.STREAM_CODEC, LinearInterpolatedVec3Provider::end,
		LinearInterpolatedVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return KayaVec3ProviderTypes.LINEAR_INTERPOLATED;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return delta().getValue(context.forChild(".delta"))
			.flatMap(delta -> start().getValue(context.forChild(".start"))
				.map(start -> end().getValue(context.forChild(".end"))
					.map(end -> start.lerp(end, delta))
					.orElse(start)));
	}

	@Override
	public void validate(ContextValidator validator) {
		Vec3Provider.super.validate(validator);
		delta().validate(validator.forChild(".delta"));
		start().validate(validator.forChild(".start"));
		end().validate(validator.forChild(".end"));
	}

}
