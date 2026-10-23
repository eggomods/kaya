package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record CheckPrecipitationCondition(Biome.Precipitation precipitation, Vec3Provider position) implements Condition {

	public static final MapCodec<CheckPrecipitationCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.PRECIPITATION.fieldOf("precipitation").forGetter(CheckPrecipitationCondition::precipitation),
		Vec3Provider.CODEC.fieldOf("position").forGetter(CheckPrecipitationCondition::position)
	).apply(instance, CheckPrecipitationCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckPrecipitationCondition> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.PRECIPITATION, CheckPrecipitationCondition::precipitation,
		Vec3Provider.STREAM_CODEC, CheckPrecipitationCondition::position,
		CheckPrecipitationCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.CHECK_PRECIPITATION;
	}

	@Override
	public boolean test(Context context) {
		Level level = context.level();
		return position().getValue(context.forChild(".position"))
			.map(BlockPos::containing)
			.filter(pos -> CachedBlock.validate(level, pos))
			.filter(pos -> level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel()) == precipitation())
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		position().validate(validator.forChild(".position"));
	}

}
