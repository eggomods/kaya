package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.damage_source.DamageSourceProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.TypeOrTagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public record CheckDamageTypeCondition(TypeOrTagPredicate<DamageType> predicate, DamageSourceProvider damageSource) implements Condition {

	public static final MapCodec<CheckDamageTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TypeOrTagPredicate.mapCodec(ResourceKey.codec(Registries.DAMAGE_TYPE), TagKey.hashedCodec(Registries.DAMAGE_TYPE)).forGetter(CheckDamageTypeCondition::predicate),
		DamageSourceProvider.CODEC.fieldOf("damage_source").forGetter(CheckDamageTypeCondition::damageSource)
	).apply(instance, CheckDamageTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckDamageTypeCondition> STREAM_CODEC = StreamCodec.composite(
		TypeOrTagPredicate.streamCodec(ResourceKey.streamCodec(Registries.DAMAGE_TYPE), TagKey.streamCodec(Registries.DAMAGE_TYPE)), CheckDamageTypeCondition::predicate,
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
			.filter(type -> predicate().test(type::is, type::is))
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		damageSource().validate(validator.forChild(".damage_source"));
	}

}
