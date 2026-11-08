package io.github.eggohito.kaya.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.entity.MutablePowers;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.registry.KayaActionTypes;
import io.github.eggohito.kaya.registry.tag.KayaTagLikes;
import io.github.eggohito.kaya.util.tag.TagLike;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record RevokePowerAction(TagLike<PowerHolder<?>> power, ResourceLocation source, EntityProvider entity) implements Action {

	public static final MapCodec<RevokePowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KayaTagLikes.POWER.codec().fieldOf("power").forGetter(RevokePowerAction::power),
		ResourceLocation.CODEC.fieldOf("source").forGetter(RevokePowerAction::source),
		EntityProvider.CODEC.fieldOf("entity").forGetter(RevokePowerAction::entity)
	).apply(instance, RevokePowerAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RevokePowerAction> STREAM_CODEC = StreamCodec.composite(
		KayaTagLikes.POWER.streamCodec(), RevokePowerAction::power,
		ResourceLocation.STREAM_CODEC, RevokePowerAction::source,
		EntityProvider.STREAM_CODEC, RevokePowerAction::entity,
		RevokePowerAction::new
	);

	@Override
	public Type<?> getType() {
		return KayaActionTypes.REVOKE_POWER;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.flatMap(MutablePowers::getOptional)
			.ifPresent(this::revoke);
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private void revoke(MutablePowers mutable) {

		try (mutable) {

			for (var holder : power().elements()) {
				mutable.revoke(holder.id(), source());
			}

		}

	}

}
