package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record CheckEntityTypeCondition(Either<EntityType<?>, TagKey<EntityType<?>>> entityType, EntityProvider entity) implements Condition {

	public static final MapCodec<CheckEntityTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.either(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), TagKey.hashedCodec(Registries.ENTITY_TYPE)).fieldOf("entity_type").forGetter(CheckEntityTypeCondition::entityType),
		EntityProvider.CODEC.fieldOf("entity").forGetter(CheckEntityTypeCondition::entity)
	).apply(instance, CheckEntityTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckEntityTypeCondition> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.either(ByteBufCodecs.registry(Registries.ENTITY_TYPE), TagKey.streamCodec(Registries.ENTITY_TYPE)), CheckEntityTypeCondition::entityType,
		EntityProvider.STREAM_CODEC, CheckEntityTypeCondition::entity,
		CheckEntityTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.CHECK_ENTITY_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.map(Entity::getType)
			.filter(type -> entityType().map(type::equals, type::is))
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		entityType().ifRight(tag -> RegistryUtil.validateTag(validator.forChild(".entity_type"), tag));
		entity().validate(validator.forChild(".entity"));
	}

}
