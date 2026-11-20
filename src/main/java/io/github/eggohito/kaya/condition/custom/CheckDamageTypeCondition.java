package io.github.eggohito.kaya.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.damage_source.DamageSourceProvider;
import io.github.eggohito.kaya.registry.KayaConditionTypes;
import io.github.eggohito.kaya.util.EitherHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public record CheckDamageTypeCondition(EitherHolder<DamageType> predicate, DamageSourceProvider damageSource) implements Condition {

	public static final MapCodec<CheckDamageTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EitherHolder.mapCodec(Registries.DAMAGE_TYPE).forGetter(CheckDamageTypeCondition::predicate),
		DamageSourceProvider.CODEC.fieldOf("damage_source").forGetter(CheckDamageTypeCondition::damageSource)
	).apply(instance, CheckDamageTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckDamageTypeCondition> STREAM_CODEC = StreamCodec.composite(
		EitherHolder.streamCodec(Registries.DAMAGE_TYPE), CheckDamageTypeCondition::predicate,
		DamageSourceProvider.STREAM_CODEC, CheckDamageTypeCondition::damageSource,
		CheckDamageTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return KayaConditionTypes.CHECK_DAMAGE_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return damageSource().getValue(context.forChild(".damage_source"))
			.stream()
			.map(DamageSource::typeHolder)
			.anyMatch(type -> predicate().keyOrTag().map(type::is, type::is));
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		damageSource().validate(validator.forChild(".damage_source"));
	}

}
