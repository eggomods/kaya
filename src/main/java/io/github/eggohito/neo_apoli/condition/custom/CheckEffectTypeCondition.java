package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.TypeOrTagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record CheckEffectTypeCondition(TypeOrTagPredicate<MobEffect> predicate, EffectProvider effect) implements Condition {

	public static final MapCodec<CheckEffectTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TypeOrTagPredicate.mapCodec(ResourceKey.codec(Registries.MOB_EFFECT), TagKey.hashedCodec(Registries.MOB_EFFECT)).forGetter(CheckEffectTypeCondition::predicate),
		EffectProvider.CODEC.fieldOf("effect").forGetter(CheckEffectTypeCondition::effect)
	).apply(instance, CheckEffectTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckEffectTypeCondition> STREAM_CODEC = StreamCodec.composite(
		TypeOrTagPredicate.streamCodec(ResourceKey.streamCodec(Registries.MOB_EFFECT), TagKey.streamCodec(Registries.MOB_EFFECT)), CheckEffectTypeCondition::predicate,
		EffectProvider.STREAM_CODEC, CheckEffectTypeCondition::effect,
		CheckEffectTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.CHECK_EFFECT_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return effect().getValue(context.forChild(".effect"))
			.map(MobEffectInstance::getEffect)
			.filter(effect -> predicate().test(effect::is, effect::is))
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		effect().validate(validator.forChild(".effect"));
	}

}
