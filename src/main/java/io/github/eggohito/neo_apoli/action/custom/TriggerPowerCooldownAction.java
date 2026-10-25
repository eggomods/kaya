package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.custom.misc.CooldownPower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record TriggerPowerCooldownAction(PowerIdentifier power, EntityProvider entity) implements Action {

	public static final MapCodec<TriggerPowerCooldownAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(TriggerPowerCooldownAction::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(TriggerPowerCooldownAction::entity)
	).apply(instance, TriggerPowerCooldownAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TriggerPowerCooldownAction> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, TriggerPowerCooldownAction::power,
		EntityProvider.STREAM_CODEC, TriggerPowerCooldownAction::entity,
		TriggerPowerCooldownAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.TRIGGER_POWER_COOLDOWN;
	}

	@Override
	public void execute(Context context) {

		Entity entity = entity().getValue(context.forChild(".entity")).orElse(null);
		Power.Instance<?> instance = Powers.getOptional(entity)
			.map(powers -> powers.getInstance(this.power()))
			.orElse(null);

		if (entity != null && instance instanceof CooldownPower.Instance<?> cooldownInstance) {
			cooldownInstance.trigger(entity);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().validate(validator.forChild(".power"), CooldownPower.class, () -> power().asDisplayString() + " doesn't have a cooldown!");
		entity().validate(validator.forChild(".entity"));
	}

}
