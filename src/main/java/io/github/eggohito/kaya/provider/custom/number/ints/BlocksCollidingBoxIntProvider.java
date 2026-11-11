package io.github.eggohito.kaya.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.parameter.BlockContextParameter;
import io.github.eggohito.kaya.provider.custom.box.BoxProvider;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.provider.KayaIntProviderTypes;
import io.github.eggohito.kaya.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record BlocksCollidingBoxIntProvider(Condition condition, BoxProvider box, Optional<EntityProvider> entity) implements IntProvider {

	public static final BlockContextParameter BLOCK_COLLIDING_BOX = new BlockContextParameter(Kaya.id("block_colliding_box"));
	public static final ContextValidator.Parameters CONDITION_PARAMETER_SET = new ContextValidator.Parameters.Builder().required(BLOCK_COLLIDING_BOX).build();

	public static final MapCodec<BlocksCollidingBoxIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(BlocksCollidingBoxIntProvider::condition),
		BoxProvider.CODEC.fieldOf("box").forGetter(BlocksCollidingBoxIntProvider::box),
		EntityProvider.CODEC.optionalFieldOf("entity").forGetter(BlocksCollidingBoxIntProvider::entity)
	).apply(instance, BlocksCollidingBoxIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlocksCollidingBoxIntProvider> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, BlocksCollidingBoxIntProvider::condition,
		BoxProvider.STREAM_CODEC, BlocksCollidingBoxIntProvider::box,
		ByteBufCodecs.optional(EntityProvider.STREAM_CODEC), BlocksCollidingBoxIntProvider::entity,
		BlocksCollidingBoxIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return KayaIntProviderTypes.BLOCKS_COLLIDING_BOX;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Context boxContext = context.forChild(".box");
		AABB box = box().getValue(boxContext).orElse(null);

		if (box == null) {
			return Optional.empty();
		}

		Level level = context.level();
		int matches = 0;

		CollisionContext collisionContext = entity()
			.flatMap(entity -> entity.getValue(context.forChild(".entity")))
			.map(CollisionContext::of)
			.orElse(CollisionContext.empty());

		BlockCollisions<BlockPos> collidedPositions = new BlockCollisions<>(
			level,
			collisionContext,
			box,
			false,
			(pos, ignored) -> pos
		);

		while (collidedPositions.hasNext()) {

			CachedBlock blockCollidingBox = CachedBlock
				.optionallyFromLoadedPos(level, collidedPositions.next())
				.orElse(null);

			if (blockCollidingBox == null) {
				continue;
			}

			Context blockContext = new Context.Builder(context)
				.withRequired(BLOCK_COLLIDING_BOX, blockCollidingBox)
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
		box().validate(validator.forChild(".box"));
		entity().ifPresent(entity -> entity.validate(validator.forChild(".entity")));
	}

}
