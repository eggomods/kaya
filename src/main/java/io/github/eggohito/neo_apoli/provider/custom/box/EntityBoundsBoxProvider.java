package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityBoundsBoxProvider(EntityProvider entity) implements BoxProvider {

	public static final MapCodec<EntityBoundsBoxProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityBoundsBoxProvider::entity)
	).apply(instance, EntityBoundsBoxProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityBoundsBoxProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, EntityBoundsBoxProvider::entity,
		EntityBoundsBoxProvider::new
	);

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.ENTITY_BOUNDS;
	}

	@Override
	public Optional<AABB> getValue(Context context) {
		return entity()
			.getValue(context.forChild(".entity"))
			.map(Entity::getBoundingBox);
	}

	@Override
	public void validate(ContextValidator validator) {
		BoxProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

	@Override
	public CollisionContext getCollisionContext(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.map(CollisionContext::of)
			.orElseGet(CollisionContext::empty);
	}

}
