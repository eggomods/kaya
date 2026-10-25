package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public record ApplyEffectsAction(List<EffectProvider> effects, EntityProvider entity) implements Action {

	public static final MapCodec<ApplyEffectsAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(EffectProvider.CODEC.listOf()).fieldOf("effects").forGetter(ApplyEffectsAction::effects),
		EntityProvider.CODEC.fieldOf("entity").forGetter(ApplyEffectsAction::entity)
	).apply(instance, ApplyEffectsAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ApplyEffectsAction> STREAM_CODEC = StreamCodec.composite(
		EffectProvider.STREAM_CODEC.apply(ByteBufCodecs.list()), ApplyEffectsAction::effects,
		EntityProvider.STREAM_CODEC, ApplyEffectsAction::entity,
		ApplyEffectsAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.APPLY_EFFECTS;
	}

	@Override
	public void execute(Context context) {

		if (!context.level().isClientSide() && entity().getValue(context.forChild(".entity")).orElse(null) instanceof LivingEntity livingEntity) {
			this.applyAll(context, livingEntity);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		ContextValidatable.validate(effects(), validator, index -> ".effects[" + index + "]");
		entity().validate(validator.forChild(".entity"));
	}

	private void applyAll(Context context, LivingEntity livingEntity) {
		MiscUtil.iterateList(
			effects(),
			(index, effect) -> effect
				.getValue(context.forChild(".effects[" + index + "]"))
				.ifPresent(livingEntity::addEffect)
		);
	}

}
