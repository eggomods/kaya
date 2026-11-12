package io.github.eggohito.kaya.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.registry.provider.KayaFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityYawFloatProvider(EntityProvider entity, FloatProvider delta) implements FloatProvider {

	public static final MapCodec<EntityYawFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityYawFloatProvider::entity),
		FloatProvider.CODEC.fieldOf("delta").forGetter(EntityYawFloatProvider::delta)
	).apply(instance, EntityYawFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityYawFloatProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, EntityYawFloatProvider::entity,
		FloatProvider.STREAM_CODEC, EntityYawFloatProvider::delta,
		EntityYawFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.ENTITY_YAW;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.flatMap(entity -> delta().getValue(context.forChild(".delta"))
				.map(entity::getViewYRot));
	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
		delta().validate(validator.forChild(".delta"));
	}

}
