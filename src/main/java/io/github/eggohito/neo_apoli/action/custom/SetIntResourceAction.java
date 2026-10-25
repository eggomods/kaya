package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.custom.IntResourcePower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SetIntResourceAction(PowerIdentifier power, EntityProvider entity, IntProvider value) implements Action {

	public static final MapCodec<SetIntResourceAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(SetIntResourceAction::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(SetIntResourceAction::entity),
		IntProvider.CODEC.fieldOf("value").forGetter(SetIntResourceAction::value)
	).apply(instance, SetIntResourceAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SetIntResourceAction> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, SetIntResourceAction::power,
		EntityProvider.STREAM_CODEC, SetIntResourceAction::entity,
		IntProvider.STREAM_CODEC, SetIntResourceAction::value,
		SetIntResourceAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.SET_RESOURCE_INT;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.ifPresent(entity -> Powers.getOptional(entity)
				.flatMap(powers -> powers.getOptionalInstance(power()))
				.filter(IntResourcePower.Instance.class::isInstance)
				.map(IntResourcePower.Instance.class::cast)
				.ifPresent(intResource -> value().getValue(context.forChild(".value"))
					.ifPresent(value -> intResource.setAndSyncValue(entity, value))));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().validate(validator.forChild(".power"), IntResourcePower.class, () -> power().asDisplayString() + " is not a resource of an int type!");
		entity().validate(validator.forChild(".entity"));
		value().validate(validator.forChild(".value"));
	}

}
