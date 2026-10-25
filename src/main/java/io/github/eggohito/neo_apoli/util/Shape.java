package io.github.eggohito.neo_apoli.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public enum Shape {

	CUBE {

		@Override
		public double getDistance(Vec3 first, Vec3 second) {
			return Math.max(Math.max(Math.abs(second.x() - first.x()), Math.abs(second.y() - first.y())), Math.abs(second.z() - first.z()));
		}

	},

	STAR {

		@Override
		public double getDistance(Vec3 first, Vec3 second) {
			return Math.abs(second.x() - first.x()) + Math.abs(second.y() - first.y()) + Math.abs(second.z() - first.z());
		}

	},

	SPHERE {

		@Override
		public double getDistance(Vec3 first, Vec3 second) {
			return Math.sqrt(Mth.square(second.x() - first.x()) + Mth.square(second.y() - first.y()) + Mth.square(second.z() - first.z()));
		}

	};

	private static final ImmutableMap<String, Shape> ALIASES = ImmutableMap.<String, Shape>builder()
		.put("chebyshev", CUBE)
		.put("manhattan", STAR)
		.put("euclidean", SPHERE)
		.build();

	public static final Codec<Shape> CODEC = CodecUtil.enumType(Shape.class, ALIASES);
	public static final StreamCodec<ByteBuf, Shape> STREAM_CODEC = StreamCodecUtil.enumType(Shape.class);

	public abstract double getDistance(Vec3 first, Vec3 second);

	public List<CachedBlock> getBlocksWithinRadius(Level level, Vec3 center, int radius) {
		double diameter = radius * 2;
		return getBlocksWithinBox(level, AABB.ofSize(center, diameter, diameter, diameter));
	}

	public List<CachedBlock> getBlocksWithinBox(Level level, AABB box) {

		List<CachedBlock> collected = new ObjectArrayList<>();
		Vec3 center = box.getCenter();

		double radius = box.getSize() / 2;

		for (var position : BlockPos.betweenClosed(box)) {

			BlockPos immutablePosition = position.immutable();
			CachedBlock block = CachedBlock
				.optionallyFromLoadedPos(level, immutablePosition)
				.orElse(null);

			if (block != null && this.getDistance(immutablePosition.getCenter(), center) <= radius) {
				collected.add(block);
			}

		}

		return collected;

	}

	public List<Entity> getEntitiesWithinRadius(Level level, Vec3 center, double radius) {
		double diameter = radius * 2;
		return getEntitiesWithinBox(level, AABB.ofSize(center, diameter, diameter, diameter));
	}

	public List<Entity> getEntitiesWithinBox(Level level, AABB box) {

		List<Entity> collected = new ObjectArrayList<>();
		Vec3 center = box.getCenter();

		double radius = box.getSize() / 2;

		for (var entity : level.getEntitiesOfClass(Entity.class, box)) {

			if (this.getDistance(entity.position(), center) <= radius) {
				collected.add(entity);
			}

		}

		return collected;

	}

}
