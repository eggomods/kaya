package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.custom.NothingAction;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.key.KeyReference;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record TogglePower(Optional<Condition> activeCondition, Action action, Optional<KeyReference> key, BooleanProvider retainState, BooleanProvider activeByDefault) implements Power {

	public static final MapCodec<TogglePower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(Action.CODEC.optionalFieldOf("action", NothingAction.INSTANCE).forGetter(TogglePower::action))
		.and(KeyReference.CODEC.optionalFieldOf("key").forGetter(TogglePower::key))
		.and(BooleanProvider.CODEC.optionalFieldOf("retain_state", new ConstantBooleanProvider(true)).forGetter(TogglePower::retainState))
		.and(BooleanProvider.CODEC.optionalFieldOf("active_by_default", new ConstantBooleanProvider(true)).forGetter(TogglePower::activeByDefault))
		.apply(instance, TogglePower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, TogglePower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		Action.STREAM_CODEC, TogglePower::action,
		ByteBufCodecs.optional(KeyReference.STREAM_CODEC), TogglePower::key,
		BooleanProvider.STREAM_CODEC, TogglePower::retainState,
		BooleanProvider.STREAM_CODEC, TogglePower::activeByDefault,
		TogglePower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.TOGGLE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {

		Power.super.validate(validator);

		action().validate(validator.forChild(".action"));
		key().ifPresent(key -> key.validate(validator.forChild(".key")));
		retainState().validate(validator.forChild(".retain_state"));
		activeByDefault().validate(validator.forChild(".active_by_default"));

	}

	public static class Instance extends Power.Instance<TogglePower> {

		private static final MapCodec<Boolean> TOGGLED_CODEC = Codec.BOOL.fieldOf("toggled");
		private boolean toggled;

		protected Instance(@NotNull TogglePower power) {
			super(power);
		}

		@Override
		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {
			return TOGGLED_CODEC.decode(ops, mapInput)
				.ifSuccess(bool -> this.toggled = bool)
				.map(ignored -> Unit.INSTANCE);
		}

		@Override
		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {
			return TOGGLED_CODEC.encode(this.toggled, ops, prefix);
		}

		@Override
		public void onGranted(Entity holder) {
			this.toggled = power.activeByDefault().getBoolean(this.createHolderContext(holder).forChild(".active_by_default"));
		}

		@Override
		public void onTick(Entity holder) {

			Context context = this.createHolderContext(holder);

			if (!super.isActive(context)) {
				this.toggle(holder, context);
			}

		}

		@Override
		public boolean shouldTick(Entity holder) {
			return toggled
				&& power.activeCondition().isPresent()
				&& !power.retainState().getBoolean(this.createHolderContext(holder).forChild(".retain_state"));
		}

		@Override
		public boolean isActive(Context context) {
			return toggled;
		}

		public Optional<KeyReference> key() {
			return power.key();
		}

		public boolean shouldToggle(Context context, KeyState previous, KeyState current) {
			return key().isPresent()
				&& previous.equals(current)
				&& current.pressed()
				&& super.isActive(context)
				&& key().get().continuouslyPressed(context, previous, current);
		}

		public void toggle(Entity holder, Context context) {

			try {

				if (context.visitor().push(this)) {

					if (!holder.level().isClientSide()) {

						this.toggled = !toggled;
						this.syncData(holder);

					}

					power.action().execute(context.forChild(".action"));

				}

			}

			finally {
				context.visitor().pop(this);
			}

		}

	}

}
