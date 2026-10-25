package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDirectionProviderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record OppositeDirectionProvider(DirectionProvider direction) implements DirectionProvider {

	public static final MapCodec<OppositeDirectionProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(DirectionProvider.CODEC.fieldOf("direction").forGetter(OppositeDirectionProvider::direction))
		.apply(instance, OppositeDirectionProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, OppositeDirectionProvider> STREAM_CODEC = StreamCodec.composite(
		DirectionProvider.STREAM_CODEC, OppositeDirectionProvider::direction,
		OppositeDirectionProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliDirectionProviderTypes.OPPOSITE;
	}

	@Override
	public Optional<Direction> getValue(Context context) {
		return direction().getValue(context.forChild(".direction")).map(Direction::getOpposite);
	}

	@Override
	public void validate(ContextValidator validator) {
		DirectionProvider.super.validate(validator);
		direction().validate(validator.forChild(".direction"));
	}

}
