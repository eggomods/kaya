package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.key.KeyPredicate;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;

public record EntityHasPressedKeyCondition(List<KeyPredicate> key, EntityProvider entity) implements Condition {

	public static final MapCodec<EntityHasPressedKeyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.compactListCodec(KeyPredicate.CODEC).fieldOf("key").forGetter(EntityHasPressedKeyCondition::key),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityHasPressedKeyCondition::entity)
	).apply(instance, EntityHasPressedKeyCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityHasPressedKeyCondition> STREAM_CODEC = StreamCodec.composite(
		KeyPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityHasPressedKeyCondition::key,
		EntityProvider.STREAM_CODEC, EntityHasPressedKeyCondition::entity,
		EntityHasPressedKeyCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ENTITY_HAS_PRESSED_KEY;
	}

	@Override
	public boolean test(Context context) {

		UUID uuid = entity().getValue(context.forChild(".entity"))
			.map(Entity::getUUID)
			.orElse(null);

		if (uuid == null) {
			return false;
		}

		ListIterator<KeyPredicate> listIterator = key().listIterator();
		long previousPressedTime = Long.MIN_VALUE;

		boolean init = false;
		int matches = 0;

		while (listIterator.hasNext()) {

			Context keyContext = context.forChild(".key[" + listIterator.nextIndex() + "]");
			Optional<KeyPredicate.State> optState = listIterator.next().getState(uuid, keyContext);

			if (optState.isEmpty()) {
				continue;
			}

			KeyPredicate.State state = optState.orElseThrow();
			long currentPressedTime = state.key().pressedTime();

			if (currentPressedTime > 0) {

				if (!init) {
					previousPressedTime = currentPressedTime;
				}

				if (currentPressedTime < (previousPressedTime - state.lowerOffset()) || currentPressedTime > (previousPressedTime + state.upperOffset())) {
					return false;
				}

				init = true;
				matches++;

			}

		}

		return matches == key.size();

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		ContextValidatable.validate(key(), validator, index -> ".key[" + index + "]");
		entity().validate(validator.forChild(".entity"));
	}

}
