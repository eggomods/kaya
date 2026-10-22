package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

public record IsSkyVisibleCondition(Vec3Provider position) implements Condition {

	public static final MapCodec<IsSkyVisibleCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Vec3Provider.CODEC.fieldOf("position").forGetter(IsSkyVisibleCondition::position))
		.apply(instance, IsSkyVisibleCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IsSkyVisibleCondition> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, IsSkyVisibleCondition::position,
		IsSkyVisibleCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_SKY_VISIBLE;
	}

	@Override
	public boolean test(Context context) {
		Level level = context.level();
		return position().getValue(context.forChild(".position"))
			.map(BlockPos::containing)
			.filter(position -> CachedBlock.validate(level, position))
			.filter(level::canSeeSky)
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		position().validate(validator.forChild(".position"));
	}

}
