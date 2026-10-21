package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record CheckEffectTypeCondition(Either<Holder<MobEffect>, TagKey<MobEffect>> effectType, EffectProvider effect) implements Condition {

	public static final MapCodec<CheckEffectTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.either(BuiltInRegistries.MOB_EFFECT.holderByNameCodec(), TagKey.hashedCodec(Registries.MOB_EFFECT)).fieldOf("effect_type").forGetter(CheckEffectTypeCondition::effectType),
		EffectProvider.CODEC.fieldOf("effect").forGetter(CheckEffectTypeCondition::effect)
	).apply(instance, CheckEffectTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckEffectTypeCondition> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.either(ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), TagKey.streamCodec(Registries.MOB_EFFECT)), CheckEffectTypeCondition::effectType,
		EffectProvider.STREAM_CODEC, CheckEffectTypeCondition::effect,
		CheckEffectTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.CHECK_EFFECT_TYPE;
	}

	@Override
	public boolean test(Context context) {
		//noinspection deprecation
		return effect().getValue(context.forChild(".effect"))
			.map(MobEffectInstance::getEffect)
			.filter(effect -> effectType().map(effect::is, effect::is))
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		effectType().ifRight(tag -> RegistryUtil.validateTag(validator.forChild(".effect_type"), tag));
		effect().validate(validator.forChild(".effect"));
	}

}
