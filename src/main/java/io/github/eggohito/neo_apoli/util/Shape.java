package io.github.eggohito.neo_apoli.util;

import com.google.common.collect.AbstractIterator;
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

import java.util.Iterator;
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
		return new ObjectArrayList<>(iterateBlocksWithinRadius(level, center, radius).iterator());
	}

	public List<Entity> getEntitiesWithinRadius(Level level, Vec3 center, double radius) {
		return new ObjectArrayList<>(iterateEntitiesWithinRadius(level, center, radius).iterator());
	}

	public List<CachedBlock> getBlocksWithinBox(Level level, AABB box) {
		return new ObjectArrayList<>(iterateBlocksWithinBox(level, box).iterator());
	}

	public List<Entity> getEntitiesWithinBox(Level level, AABB box) {
		return new ObjectArrayList<>(iterateEntitiesWithinBox(level, box).iterator());
	}

	public Iterable<CachedBlock> iterateBlocksWithinRadius(Level level, Vec3 center, int radius) {
		double diameter = radius * 2;
		return iterateBlocksWithinBox(level, AABB.ofSize(center, diameter, diameter, diameter));
	}

	public Iterable<Entity> iterateEntitiesWithinRadius(Level level, Vec3 center, double radius) {
		double diameter = radius * 2;
		return iterateEntitiesWithinBox(level, AABB.ofSize(center, diameter, diameter, diameter));
	}

	public Iterable<CachedBlock> iterateBlocksWithinBox(Level level, AABB box) {
		return () -> new ShapeIterator<>(box) {

			final Iterator<BlockPos> iterator = BlockPos.betweenClosed(box).iterator();

			@Override
			protected CachedBlock computeNext() {

				while (iterator.hasNext()) {

					BlockPos pos = iterator.next();
					CachedBlock block = CachedBlock.optionallyFromLoadedPos(level, pos).orElse(null);

					if (block != null && Shape.this.getDistance(pos.getCenter(), center) <= radius) {
						return block;
					}

				}

				return endOfData();

			}

		};
	}

	public Iterable<Entity> iterateEntitiesWithinBox(Level level, AABB box) {
		return () -> new ShapeIterator<>(box) {

			final Iterator<Entity> iterator = level.getEntitiesOfClass(Entity.class, box).iterator();

			@Override
			protected Entity computeNext() {

				while (iterator.hasNext()) {

					Entity entity = iterator.next();
					double distance = Shape.this.getDistance(entity.position(), center);

					if (distance <= radius) {
						return entity;
					}

				}

				return endOfData();

			}

		};
	}

	private static abstract class ShapeIterator<T> extends AbstractIterator<T> {

		protected final Vec3 center;
		protected final double radius;

		protected ShapeIterator(AABB box) {
			this.center = box.getCenter();
			this.radius = box.getSize() / 2;
		}

	}

}
