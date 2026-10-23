package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.TypeOrTagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record CheckEntityTypeCondition(TypeOrTagPredicate<EntityType<?>> predicate, EntityProvider entity) implements Condition {

	public static final MapCodec<CheckEntityTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TypeOrTagPredicate.mapCodec(ResourceKey.codec(Registries.ENTITY_TYPE), TagKey.hashedCodec(Registries.ENTITY_TYPE)).forGetter(CheckEntityTypeCondition::predicate),
		EntityProvider.CODEC.fieldOf("entity").forGetter(CheckEntityTypeCondition::entity)
	).apply(instance, CheckEntityTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckEntityTypeCondition> STREAM_CODEC = StreamCodec.composite(
		TypeOrTagPredicate.streamCodec(ResourceKey.streamCodec(Registries.ENTITY_TYPE), TagKey.streamCodec(Registries.ENTITY_TYPE)), CheckEntityTypeCondition::predicate,
		EntityProvider.STREAM_CODEC, CheckEntityTypeCondition::entity,
		CheckEntityTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.CHECK_ENTITY_TYPE;
	}

	@Override
	public boolean test(Context context) {
		//noinspection deprecation
		return entity().getValue(context.forChild(".entity"))
			.map(Entity::getType)
			.filter(type -> predicate().test(type.builtInRegistryHolder()::is, type::is))
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}
