package io.github.eggohito.kaya.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.effect.EffectProvider;
import io.github.eggohito.kaya.registry.KayaConditionTypes;
import io.github.eggohito.kaya.util.EitherHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record CheckEffectTypeCondition(EitherHolder<MobEffect> predicate, EffectProvider effect) implements Condition {

	public static final MapCodec<CheckEffectTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EitherHolder.mapCodec(Registries.MOB_EFFECT).forGetter(CheckEffectTypeCondition::predicate),
		EffectProvider.CODEC.fieldOf("effect").forGetter(CheckEffectTypeCondition::effect)
	).apply(instance, CheckEffectTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckEffectTypeCondition> STREAM_CODEC = StreamCodec.composite(
		EitherHolder.streamCodec(Registries.MOB_EFFECT), CheckEffectTypeCondition::predicate,
		EffectProvider.STREAM_CODEC, CheckEffectTypeCondition::effect,
		CheckEffectTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return KayaConditionTypes.CHECK_EFFECT_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return effect().getValue(context.forChild(".effect"))
			.stream()
			.map(MobEffectInstance::getEffect)
			.anyMatch(effect -> predicate().keyOrTag().map(effect::is, effect::is));
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		effect().validate(validator.forChild(".effect"));
	}

}
