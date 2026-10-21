package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.key.manager.KeyStateManager;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
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

		while (listIterator.hasNext()) {

			Context keyContext = context.forChild(".key[" + listIterator.nextIndex() + "]");
			KeyPredicate keyPredicate = listIterator.next();

			Optional<KeyState> optState = keyPredicate.id()
				.getValue(keyContext)
				.flatMap(id -> KeyStateManager.getInstance().getCurrentState(uuid, id));

			if (optState.isEmpty()) {
				continue;
			}

			Context timingWindowContext = keyContext.forChild(".timing_window");

			int min = keyPredicate.timingWindow().min().getInt(timingWindowContext.forChild(".min"));
			int max = keyPredicate.timingWindow().max().getInt(timingWindowContext.forChild(".max"));

			KeyState state = optState.get();
			long currentPressedTime = state.pressedTime();

			if (state.pressed()) {

				if (previousPressedTime == Long.MIN_VALUE) {
					previousPressedTime = currentPressedTime;
				}

				else if (currentPressedTime < (previousPressedTime - min) || currentPressedTime > (previousPressedTime + max)) {
					return false;
				}

			}

		}

		return true;

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		ContextValidatable.validate(key(), validator, index -> ".key[" + index + "]");
		entity().validate(validator.forChild(".entity"));
	}

	public record KeyPredicate(StringProvider id, TimingWindow timingWindow) implements ContextValidatable {

		public static final Codec<KeyPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			StringProvider.CODEC.fieldOf("id").forGetter(KeyPredicate::id),
			TimingWindow.CODEC.optionalFieldOf("timing_window", new TimingWindow(3, 3)).forGetter(KeyPredicate::timingWindow)
		).apply(instance, KeyPredicate::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, KeyPredicate> STREAM_CODEC = StreamCodec.composite(
			StringProvider.STREAM_CODEC, KeyPredicate::id,
			TimingWindow.STREAM_CODEC, KeyPredicate::timingWindow,
			KeyPredicate::new
		);

		@Override
		public void validate(ContextValidator validator) {
			id().validate(validator.forChild(".id"));
			timingWindow().validate(validator.forChild(".timing_window"));
		}

	}

	public record TimingWindow(IntProvider min, IntProvider max) implements ContextValidatable {

		public static final Codec<TimingWindow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IntProvider.CODEC.fieldOf("min").forGetter(TimingWindow::min),
			IntProvider.CODEC.fieldOf("max").forGetter(TimingWindow::max)
		).apply(instance, TimingWindow::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, TimingWindow> STREAM_CODEC = StreamCodec.composite(
			IntProvider.STREAM_CODEC, TimingWindow::min,
			IntProvider.STREAM_CODEC, TimingWindow::max,
			TimingWindow::new
		);

		public TimingWindow(int min, int max) {
			this(new ConstantIntProvider(min), new ConstantIntProvider(max));
		}

		@Override
		public void validate(ContextValidator validator) {
			min().validate(validator.forChild(".min"));
			max().validate(validator.forChild(".max"));
		}

	}

}
