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

public record GrantPowerAction(TagLike<PowerHolder<?>> power, ResourceLocation source, EntityProvider entity) implements Action {

	public static final MapCodec<GrantPowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KayaTagLikes.POWER.codec().fieldOf("power").forGetter(GrantPowerAction::power),
		ResourceLocation.CODEC.fieldOf("source").forGetter(GrantPowerAction::source),
		EntityProvider.CODEC.fieldOf("entity").forGetter(GrantPowerAction::entity)
	).apply(instance, GrantPowerAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, GrantPowerAction> STREAM_CODEC = StreamCodec.composite(
		KayaTagLikes.POWER.streamCodec(), GrantPowerAction::power,
		ResourceLocation.STREAM_CODEC, GrantPowerAction::source,
		EntityProvider.STREAM_CODEC, GrantPowerAction::entity,
		GrantPowerAction::new
	);

	@Override
	public Type<?> getType() {
		return KayaActionTypes.GRANT_POWER;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.map(MutablePowers::create)
			.ifPresent(this::grant);
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private void grant(MutablePowers mutable) {

		try (mutable) {

			for (var holder : power().elements()) {
				mutable.grant(holder.id(), source());
			}

		}

	}

}
