package io.github.eggohito.kaya.condition.custom;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.block.BlockProvider;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.kaya.registry.KayaConditionTypes;
import io.github.eggohito.kaya.util.AttributeUtil;
import io.github.eggohito.kaya.util.CachedBlock;
import io.github.eggohito.kaya.util.OptionalUtil;
import io.github.eggohito.kaya.util.alias.CustomRegistryAlias;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record IsEntityLookingAtCondition(EntityProvider viewer, Target target, FloatProvider coneSize, BooleanProvider adjustForDistance, ClipContext.Block blockHandling, ClipContext.Fluid fluidHandling) implements Condition {

	public static final MapCodec<IsEntityLookingAtCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("viewer").forGetter(IsEntityLookingAtCondition::viewer),
		Target.CODEC.fieldOf("target").forGetter(IsEntityLookingAtCondition::target),
		FloatProvider.clamped(0.0F, Float.MAX_VALUE).optionalFieldOf("cone_size", new ConstantFloatProvider(0.5F)).forGetter(IsEntityLookingAtCondition::coneSize),
		BooleanProvider.CODEC.optionalFieldOf("adjust_for_distance", ConstantBooleanProvider.TRUE).forGetter(IsEntityLookingAtCondition::adjustForDistance),
		KayaCodecs.BLOCK_CLIP_CONTEXT.optionalFieldOf("block_handling", ClipContext.Block.VISUAL).forGetter(IsEntityLookingAtCondition::blockHandling),
		KayaCodecs.FLUID_CLIP_CONTEXT.optionalFieldOf("fluid_handling", ClipContext.Fluid.NONE).forGetter(IsEntityLookingAtCondition::fluidHandling)
	).apply(instance, IsEntityLookingAtCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEntityLookingAtCondition> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, IsEntityLookingAtCondition::viewer,
		Target.STREAM_CODEC, IsEntityLookingAtCondition::target,
		FloatProvider.STREAM_CODEC, IsEntityLookingAtCondition::coneSize,
		BooleanProvider.STREAM_CODEC, IsEntityLookingAtCondition::adjustForDistance,
		KayaStreamCodecs.BLOCK_CLIP_CONTEXT, IsEntityLookingAtCondition::blockHandling,
		KayaStreamCodecs.FLUID_CLIP_CONTEXT, IsEntityLookingAtCondition::fluidHandling,
		IsEntityLookingAtCondition::new
	);

	@Override
	public Type<?> getType() {
		return KayaConditionTypes.IS_ENTITY_LOOKING_AT;
	}

	@Override
	public boolean test(Context context) {
		return viewer().getValue(context.forChild(".viewer"))
			.flatMap(OptionalUtil.flatCheckAndCast(LivingEntity.class))
			.flatMap(viewer -> coneSize().getValue(context.forChild(".cone_size"))
				.flatMap(coneSize -> adjustForDistance().getValue(context.forChild(".adjust_for_distance"))
					.map(adjustForDistance -> target().isBeingLookedAt(context, viewer, coneSize, adjustForDistance, blockHandling(), fluidHandling())))).orElse(false);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		viewer().validate(validator.forChild(".viewer"));
		target().validate(validator.forChild(".target"));
		coneSize().validate(validator.forChild(".cone_size"));
		adjustForDistance().validate(validator.forChild(".adjust_for_distance"));
	}

	public sealed interface Target extends ContextValidatable {

		Codec<Target> CODEC = Type.CODEC.dispatch(Target::getType, Type::mapCodec);

		StreamCodec<RegistryFriendlyByteBuf, Target> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Target::getType, Type::streamCodec);

		Type<?> getType();

		boolean isBeingLookedAt(Context context, LivingEntity viewer, double coneSize, boolean adjustForDistance, ClipContext.Block blockHandling, ClipContext.Fluid fluidHandling);

		static boolean withinCone(Vec3 view, Vec3 delta, double coneSize, boolean adjustForDistance) {
			return view.dot(delta.normalize()) > 1.0 - coneSize / (adjustForDistance ? delta.length() : 1.0);
		}

		record Type<T extends Target>(MapCodec<T> mapCodec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

			private static final ImmutableBiMap<ResourceLocation, Type<?>> VALUES = ImmutableBiMap.of(
				Kaya.id("entity"), EntityTarget.TYPE,
				Kaya.id("block"), BlockTarget.TYPE
			);

			public static final CustomRegistryAlias<Type<?>> ALIASES = new CustomRegistryAlias<>(Kaya.id("looking_at_target"), VALUES);

			public static final Codec<Type<?>> CODEC = ALIASES.createCodec();

			public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createStreamCodec();

		}

	}

	public record EntityTarget(EntityProvider entity, BooleanProvider eyesOnly) implements Target {

		public static final MapCodec<EntityTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			EntityProvider.CODEC.fieldOf("entity").forGetter(EntityTarget::entity),
			BooleanProvider.CODEC.optionalFieldOf("eyes_only", ConstantBooleanProvider.FALSE).forGetter(EntityTarget::eyesOnly)
		).apply(instance, EntityTarget::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, EntityTarget> STREAM_CODEC = StreamCodec.composite(
			EntityProvider.STREAM_CODEC, EntityTarget::entity,
			BooleanProvider.STREAM_CODEC, EntityTarget::eyesOnly,
			EntityTarget::new
		);

		public static final Type<EntityTarget> TYPE = new Type<>(CODEC, STREAM_CODEC);

		@Override
		public Type<?> getType() {
			return TYPE;
		}

		@Override
		public boolean isBeingLookedAt(Context context, LivingEntity viewer, double coneSize, boolean adjustForDistance, ClipContext.Block blockHandling, ClipContext.Fluid fluidHandling) {
			return entity().getValue(context.forChild(".entity"))
				.flatMap(entity -> eyesOnly().getValue(context.forChild(".eyes_only"))
					.map(eyesOnly -> isLookingAtEntity(viewer, entity, eyesOnly, coneSize, adjustForDistance, blockHandling, fluidHandling))).orElse(false);
		}

		@Override
		public void validate(ContextValidator validator) {
			entity().validate(validator.forChild(".entity"));
			eyesOnly().validate(validator.forChild(".eyes_only"));
		}

		private static DoubleList getYOffsets(Entity entity, boolean eyesOnly) {

			DoubleList yOffsets = new DoubleArrayList();
			yOffsets.add(entity.getEyeY());

			if (!eyesOnly) {
				yOffsets.add(entity.getY() + 0.5 * AttributeUtil.getValue(entity, Attributes.SCALE, () -> 1.0));
				yOffsets.add((entity.getEyeY() + entity.getY()) / 2);
			}

			return yOffsets;

		}

		/**
		 *  A modified version of {@link LivingEntity#isLookingAtMe(LivingEntity, double, boolean, boolean, double...)} with customizable raycast block/fluid handling
		 */
		private static boolean isLookingAtEntity(LivingEntity viewer, Entity entity, boolean eyesOnly, double coneSize, boolean adjustForDistance, ClipContext.Block blockHandling, ClipContext.Fluid fluidHandling) {

			Vec3 view = viewer.getViewVector(1.0F).normalize();
			DoubleList yOffsets = getYOffsets(entity, eyesOnly);

			for (double yOffset : yOffsets) {

				Vec3 delta = entity.position()
					.with(Direction.Axis.Y, yOffset)
					.subtract(viewer.getEyePosition());

				if (Target.withinCone(view, delta, coneSize, adjustForDistance) && viewer.hasLineOfSight(entity, blockHandling, fluidHandling, yOffset)) {
					return true;
				}

			}

			return false;

		}

	}

	public record BlockTarget(BlockProvider block) implements Target {

		public static final MapCodec<BlockTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(BlockProvider.CODEC.fieldOf("block").forGetter(BlockTarget::block))
			.apply(instance, BlockTarget::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, BlockTarget> STREAM_CODEC = BlockProvider.STREAM_CODEC.map(
			BlockTarget::new,
			BlockTarget::block
		);

		public static final Type<BlockTarget> TYPE = new Type<>(CODEC, STREAM_CODEC);

		@Override
		public Type<?> getType() {
			return TYPE;
		}

		@Override
		public boolean isBeingLookedAt(Context context, LivingEntity viewer, double coneSize, boolean adjustForDistance, ClipContext.Block blockHandling, ClipContext.Fluid fluidHandling) {
			return block().getValue(context.forChild(".block"))
				.map(block -> isLookingAtBlock(context.level(), viewer, block, coneSize, adjustForDistance, blockHandling, fluidHandling))
				.orElse(false);
		}

		@Override
		public void validate(ContextValidator validator) {
			block().validate(validator.forChild(".block"));
		}

		private static boolean isLookingAtBlock(Level level, LivingEntity viewer, CachedBlock block, double coneSize, boolean adjustForDistance, ClipContext.Block blockHandling, ClipContext.Fluid fluidHandling) {

			Vec3 view = viewer.getViewVector(1.0F).normalize();
			Vec3 pos = block.pos().getCenter();

			Vec3 eyePos = viewer.getEyePosition();
			Vec3 delta = pos.subtract(eyePos);

			if (!Target.withinCone(view, delta, coneSize, adjustForDistance)) {
				return false;
			}

			BlockHitResult blockResult = level.clip(new ClipContext(eyePos, pos, blockHandling, fluidHandling, viewer));
			Vec3 destination = blockResult.getBlockPos().getCenter();

			return blockResult.getType() != HitResult.Type.MISS
				&& destination.equals(pos);

		}

	}

}
