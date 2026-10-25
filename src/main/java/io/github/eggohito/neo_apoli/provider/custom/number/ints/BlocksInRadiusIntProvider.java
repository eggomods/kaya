package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.Shape;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record BlocksInRadiusIntProvider(Condition condition, Vec3Provider position, Shape shape, IntProvider radius) implements IntProvider {

	public static final BlockContextParameter BLOCK_IN_RADIUS = new BlockContextParameter(NeoApoli.id("block_in_radius"));
	public static final ContextValidator.Parameters CONDITION_PARAMETER_SET = new ContextValidator.Parameters.Builder().required(BLOCK_IN_RADIUS).build();

	public static final MapCodec<BlocksInRadiusIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(BlocksInRadiusIntProvider::condition),
		Vec3Provider.CODEC.fieldOf("position").forGetter(BlocksInRadiusIntProvider::position),
		Shape.CODEC.fieldOf("shape").forGetter(BlocksInRadiusIntProvider::shape),
		IntProvider.CODEC.fieldOf("radius").forGetter(BlocksInRadiusIntProvider::radius)
	).apply(instance, BlocksInRadiusIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlocksInRadiusIntProvider> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, BlocksInRadiusIntProvider::condition,
		Vec3Provider.STREAM_CODEC, BlocksInRadiusIntProvider::position,
		Shape.STREAM_CODEC, BlocksInRadiusIntProvider::shape,
		IntProvider.STREAM_CODEC, BlocksInRadiusIntProvider::radius,
		BlocksInRadiusIntProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliIntProviderTypes.BLOCKS_IN_RADIUS;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Vec3 position = position()
			.getValue(context.forChild(".position"))
			.orElse(null);

		if (position == null) {
			return Optional.empty();
		}

		Level level = context.level();
		int matches = 0;

		int radius = radius().getInt(context.forChild(".radius"));
		List<CachedBlock> blocks = shape().getBlocksWithinRadius(level, position, radius);

		for (var block : blocks) {

			Context blockContext = new Context.Builder(context)
				.withRequired(BLOCK_IN_RADIUS, block)
				.build(level);

			if (condition().test(blockContext.forChild(".condition"))) {
				matches++;
			}

		}

		return Optional.of(matches);

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		condition().validate(validator.withParams(CONDITION_PARAMETER_SET).forChild(".condition"));
		position().validate(validator.forChild(".position"));
		radius().validate(validator.forChild(".radius"));
	}

}
