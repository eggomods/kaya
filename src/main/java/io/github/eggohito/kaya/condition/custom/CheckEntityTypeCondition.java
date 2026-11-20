package io.github.eggohito.kaya.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.registry.KayaConditionTypes;
import io.github.eggohito.kaya.util.EitherHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record CheckEntityTypeCondition(EitherHolder<EntityType<?>> predicate, EntityProvider entity) implements Condition {

	public static final MapCodec<CheckEntityTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EitherHolder.mapCodec(Registries.ENTITY_TYPE).forGetter(CheckEntityTypeCondition::predicate),
		EntityProvider.CODEC.fieldOf("entity").forGetter(CheckEntityTypeCondition::entity)
	).apply(instance, CheckEntityTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckEntityTypeCondition> STREAM_CODEC = StreamCodec.composite(
		EitherHolder.streamCodec(Registries.ENTITY_TYPE), CheckEntityTypeCondition::predicate,
		EntityProvider.STREAM_CODEC, CheckEntityTypeCondition::entity,
		CheckEntityTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return KayaConditionTypes.CHECK_ENTITY_TYPE;
	}

	@Override
	public boolean test(Context context) {
		//noinspection deprecation
		return entity().getValue(context.forChild(".entity"))
			.stream()
			.map(Entity::getType)
			.map(EntityType::builtInRegistryHolder)
			.anyMatch(type -> predicate().keyOrTag().map(type::is, type::is));
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}
