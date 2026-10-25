package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record DistanceBetweenVectorsFloatProvider(Vec3Provider first, Vec3Provider second) implements FloatProvider {

	public static final MapCodec<DistanceBetweenVectorsFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("first").forGetter(DistanceBetweenVectorsFloatProvider::first),
		Vec3Provider.CODEC.fieldOf("second").forGetter(DistanceBetweenVectorsFloatProvider::second)
	).apply(instance, DistanceBetweenVectorsFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DistanceBetweenVectorsFloatProvider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, DistanceBetweenVectorsFloatProvider::first,
		Vec3Provider.STREAM_CODEC, DistanceBetweenVectorsFloatProvider::second,
		DistanceBetweenVectorsFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.DISTANCE_BETWEEN_VECTORS;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return first().getValue(context.forChild(".first"))
			.map(Vec3::toVector3f)
			.flatMap(first -> second().getValue(context.forChild(".second"))
				.map(Vec3::toVector3f)
				.map(first::distance));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		first().validate(validator.forChild(".first"));
		second().validate(validator.forChild(".second"));
	}

}
