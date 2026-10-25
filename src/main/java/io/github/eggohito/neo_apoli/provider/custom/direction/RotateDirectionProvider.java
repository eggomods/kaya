package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDirectionProviderTypes;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RotateDirectionProvider(Orientation orientation, DirectionProvider direction, Direction.Axis axis) implements DirectionProvider {

	public static final MapCodec<RotateDirectionProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Orientation.CODEC.fieldOf("orientation").forGetter(RotateDirectionProvider::orientation),
		DirectionProvider.CODEC.fieldOf("direction").forGetter(RotateDirectionProvider::direction),
		Direction.Axis.CODEC.fieldOf("axis").forGetter(RotateDirectionProvider::axis)
	).apply(instance, RotateDirectionProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RotateDirectionProvider> STREAM_CODEC = StreamCodec.composite(
		Orientation.STREAM_CODEC, RotateDirectionProvider::orientation,
		DirectionProvider.STREAM_CODEC, RotateDirectionProvider::direction,
		NeoApoliStreamCodecs.AXIS, RotateDirectionProvider::axis,
		RotateDirectionProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliDirectionProviderTypes.ROTATE;
	}

	@Override
	public Optional<Direction> getValue(Context context) {
		return direction()
			.getValue(context.forChild(".direction"))
			.map(direction -> orientation().rotate(direction, axis()));
	}

	@Override
	public void validate(ContextValidator validator) {
		DirectionProvider.super.validate(validator);
		direction().validate(validator.forChild(".direction"));
	}

	public enum Orientation {

		CLOCKWISE,
		COUNTER_CLOCKWISE;

		public static final Codec<Orientation> CODEC = CodecUtil.enumType(Orientation.class);
		public static final StreamCodec<ByteBuf, Orientation> STREAM_CODEC = StreamCodecUtil.enumType(Orientation.class);

		public Direction rotate(Direction direction, Direction.Axis axis) {
			return switch (this) {
				case CLOCKWISE ->
					direction.getClockWise(axis);
				case COUNTER_CLOCKWISE ->
					direction.getCounterClockWise(axis);
			};
		}

	}

}
