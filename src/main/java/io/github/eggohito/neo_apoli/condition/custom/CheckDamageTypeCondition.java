package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.damage_source.DamageSourceProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public record CheckDamageTypeCondition(Either<ResourceKey<DamageType>, TagKey<DamageType>> damageType, DamageSourceProvider damageSource) implements Condition {

	public static final MapCodec<CheckDamageTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.either(ResourceKey.codec(Registries.DAMAGE_TYPE), TagKey.hashedCodec(Registries.DAMAGE_TYPE)).fieldOf("damage_type").forGetter(CheckDamageTypeCondition::damageType),
		DamageSourceProvider.CODEC.fieldOf("damage_source").forGetter(CheckDamageTypeCondition::damageSource)
	).apply(instance, CheckDamageTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckDamageTypeCondition> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.either(ResourceKey.streamCodec(Registries.DAMAGE_TYPE), TagKey.streamCodec(Registries.DAMAGE_TYPE)), CheckDamageTypeCondition::damageType,
		DamageSourceProvider.STREAM_CODEC, CheckDamageTypeCondition::damageSource,
		CheckDamageTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.CHECK_DAMAGE_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return damageSource().getValue(context.forChild(".damage_source"))
			.map(DamageSource::typeHolder)
			.filter(type -> damageType().map(type::is, type::is))
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		RegistryUtil.validateKeyOrTag(validator.forChild(".damage_type"), damageType());
		damageSource().validate(validator.forChild(".damage_source"));
	}

}
