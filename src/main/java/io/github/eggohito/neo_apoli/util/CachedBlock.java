package io.github.eggohito.neo_apoli.util;

import io.github.eggohito.neo_apoli.exception.PosOutOfBoundsException;
import io.github.eggohito.neo_apoli.exception.PosUnloadedException;
import io.github.eggohito.neo_apoli.mixin.access.BlockInWorldAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("deprecation")
public record CachedBlock(BlockPos pos, BlockState state, @Nullable BlockEntity entity) {

	public static Optional<CachedBlock> optionallyFromLoadedPos(Level level, BlockPos pos) {
		return Optional.of(pos)
			.filter(_pos -> validate(level, _pos))
			.map(_pos -> new CachedBlock(level, _pos));
	}

	@SuppressWarnings("ConstantValue")
	public static Optional<CachedBlock> optionallyFromWorld(BlockInWorld block) {
		return (((BlockInWorldAccessor) block).doesLoadChunks() || block.getState() != null)
			? Optional.of(new CachedBlock(block.getPos(), block.getState(), block.getEntity()))
			: Optional.empty();
	}

	public static CachedBlock fromLoadedPos(Level level, BlockPos pos) {

		if (!level.hasChunkAt(pos)) {
			throw new PosUnloadedException(level, pos);
		}

		else if (!level.isInWorldBounds(pos)) {
			throw new PosOutOfBoundsException(level, pos);
		}

		else {
			return new CachedBlock(level, pos);
		}

	}

	public static boolean validate(Level level, BlockPos pos) {
		return level.hasChunkAt(pos)
			&& level.isInWorldBounds(pos);
	}

	public CachedBlock(Level level, BlockPos pos) {
		this(pos, level.getBlockState(pos), level.getBlockEntity(pos));
	}

}
