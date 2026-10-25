package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.custom.FloatResourcePower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SetFloatResourceAction(PowerIdentifier power, EntityProvider entity, FloatProvider value) implements Action {

	public static final MapCodec<SetFloatResourceAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(SetFloatResourceAction::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(SetFloatResourceAction::entity),
		FloatProvider.CODEC.fieldOf("value").forGetter(SetFloatResourceAction::value)
	).apply(instance, SetFloatResourceAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SetFloatResourceAction> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, SetFloatResourceAction::power,
		EntityProvider.STREAM_CODEC, SetFloatResourceAction::entity,
		FloatProvider.STREAM_CODEC, SetFloatResourceAction::value,
		SetFloatResourceAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.SET_RESOURCE_FLOAT;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.ifPresent(entity -> Powers.getOptional(entity)
				.flatMap(powers -> powers.getOptionalInstance(power()))
				.filter(FloatResourcePower.Instance.class::isInstance)
				.map(FloatResourcePower.Instance.class::cast)
				.ifPresent(floatResource -> value().getValue(context.forChild(".value"))
					.ifPresent(value -> floatResource.setAndSyncValue(entity, value))));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().validate(validator.forChild(".power"), FloatResourcePower.class, () -> power().asDisplayString() + " is not a resource of a float type!");
		entity().validate(validator.forChild(".entity"));
		value().validate(validator.forChild(".value"));
	}

}
