package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityAttributeFloatProvider(Holder<Attribute> attribute, EntityProvider entity) implements FloatProvider {

	public static final MapCodec<EntityAttributeFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Attribute.CODEC.fieldOf("attribute").forGetter(EntityAttributeFloatProvider::attribute),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityAttributeFloatProvider::entity)
	).apply(instance, EntityAttributeFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityAttributeFloatProvider> STREAM_CODEC = StreamCodec.composite(
		Attribute.STREAM_CODEC, EntityAttributeFloatProvider::attribute,
		EntityProvider.STREAM_CODEC, EntityAttributeFloatProvider::entity,
		EntityAttributeFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.ENTITY_ATTRIBUTE;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		Context entityContext = context.forChild(".entity");
		Entity entity = entity().getValue(entityContext).orElse(null);

		switch (entity) {
			case LivingEntity livingEntity -> {

				if (livingEntity.getAttributes().hasAttribute(attribute())) {
					return Optional.of((float) livingEntity.getAttributeValue(attribute()));
				}

			}
			case null ->
				entityContext.reportProblem("Entity doesn't exist!");
			default ->
				entityContext.reportProblem("Entity can't have attributes!");
		}

		return Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}
