package io.github.eggohito.neo_apoli.area.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.area.Area;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.area.NeoApoliAreaTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;

public record RadiusArea(Area.Settings settings, Vec3Provider position, FloatProvider radius) implements Area {

	public static final MapCodec<RadiusArea> CODEC = RecordCodecBuilder.mapCodec(instance ->
		Area.addSettingsField(instance)
			.and(Vec3Provider.CODEC.fieldOf("position").forGetter(RadiusArea::position))
			.and(FloatProvider.CODEC.fieldOf("radius").forGetter(RadiusArea::radius))
			.apply(instance, RadiusArea::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, RadiusArea> STREAM_CODEC = StreamCodec.composite(
		Settings.STREAM_CODEC, RadiusArea::settings,
		Vec3Provider.STREAM_CODEC, RadiusArea::position,
		FloatProvider.STREAM_CODEC, RadiusArea::radius,
		RadiusArea::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliAreaTypes.RADIUS;
	}

	@Override
	public Iterable<Entity> iterateEntities(Context context) {
		return unwrap(context, (position, radius) -> settings().shape().iterateEntitiesWithinRadius(context.level(), position, radius));
	}

	@Override
	public Iterable<CachedBlock> iterateBlocks(Context context) {
		return unwrap(context, (position, radius) -> settings().shape().iterateBlocksWithinRadius(context.level(), position, Math.round(radius)));
	}

	@Override
	public void validate(ContextValidator validator) {
		position().validate(validator.forChild(".position"));
		radius().validate(validator.forChild(".radius"));
	}

	private <R> Iterable<R> unwrap(Context context, BiFunction<Vec3, Float, Iterable<R>> applier) {
		return position().getValue(context.forChild(".position"))
			.flatMap(position -> radius().getValue(context.forChild(".radius"))
				.map(radius -> applier.apply(position, radius))).orElse(MiscUtil.emptyIterable());
	}

}
