package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record GainAirAction(IntProvider value, EntityProvider entity) implements Action {

	public static final MapCodec<GainAirAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.fieldOf("value").forGetter(GainAirAction::value),
		EntityProvider.CODEC.fieldOf("entity").forGetter(GainAirAction::entity)
	).apply(instance, GainAirAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, GainAirAction> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, GainAirAction::value,
		EntityProvider.STREAM_CODEC, GainAirAction::entity,
		GainAirAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.GAIN_AIR;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.filter(Player.class::isInstance)
			.map(Player.class::cast)
			.ifPresent(player -> player.setAirSupply(Math.min(player.getAirSupply() + value().getInt(context.forChild(".value")), player.getMaxAirSupply())));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		value().validate(validator.forChild(".value"));
		entity().validate(validator.forChild(".entity"));
	}

}
