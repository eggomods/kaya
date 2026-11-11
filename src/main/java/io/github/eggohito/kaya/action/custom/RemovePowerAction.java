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

public record RemovePowerAction(TagLike<PowerHolder<?>> power, EntityProvider entity) implements Action {

	public static final MapCodec<RemovePowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KayaTagLikes.POWER.codec().fieldOf("power").forGetter(RemovePowerAction::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(RemovePowerAction::entity)
	).apply(instance, RemovePowerAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RemovePowerAction> STREAM_CODEC = StreamCodec.composite(
		KayaTagLikes.POWER.streamCodec(), RemovePowerAction::power,
		EntityProvider.STREAM_CODEC, RemovePowerAction::entity,
		RemovePowerAction::new
	);

	@Override
	public Type<?> getType() {
		return KayaActionTypes.REMOVE_POWER;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.flatMap(MutablePowers::getOptional)
			.ifPresent(this::remove);
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private void remove(MutablePowers mutable) {

		try (mutable) {

			for (var holder : power().elements()) {

				for (var source : mutable.getSources(holder.id())) {
					mutable.revoke(holder.id(), source);
				}

			}

		}
	}

}
