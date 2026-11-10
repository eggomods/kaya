package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.parameter.BlockContextParameter;
import io.github.eggohito.kaya.context.parameter.EntityContextParameter;
import io.github.eggohito.kaya.context.parameter.Vec3ContextParameter;
import io.github.eggohito.kaya.hud.element.HudElement;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.CooldownPower;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

public record CallbackVibrationListenedPower(HudElement hudElement, IntProvider cooldown, Action onListenedAction, Optional<HolderSet<GameEvent>> vibration, IntProvider range, Properties properties) implements CooldownPower {

	public static final Vec3ContextParameter VIBRATION_POSITION = new Vec3ContextParameter(Kaya.id("vibration/position"));
	public static final BlockContextParameter VIBRATION_AFFECTED_BLOCK = new BlockContextParameter(Kaya.id("vibration/affected_block"));
	public static final EntityContextParameter VIBRATION_EMITTER = new EntityContextParameter(Kaya.id("vibration/emitter"));
	public static final EntityContextParameter VIBRATION_PROJECTILE_OWNER = new EntityContextParameter(Kaya.id("vibration/projectile_owner"));

	public static final MapCodec<CallbackVibrationListenedPower> CODEC = RecordCodecBuilder.mapCodec(instance -> CooldownPower
		.addFields(instance, HudElement.CODEC, IntProvider.clamped(1, Integer.MAX_VALUE))
		.and(Action.CODEC.fieldOf("on_listened_action").forGetter(CallbackVibrationListenedPower::onListenedAction))
		.and(RegistryCodecs.homogeneousList(Registries.GAME_EVENT).optionalFieldOf("vibration").forGetter(CallbackVibrationListenedPower::vibration))
		.and(IntProvider.clamped(1, Integer.MAX_VALUE).fieldOf("range").forGetter(CallbackVibrationListenedPower::cooldown))
		.and(Power.propertiesCodecBuilder(true))
		.apply(instance, CallbackVibrationListenedPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackVibrationListenedPower> STREAM_CODEC = StreamCodec.composite(
		HudElement.STREAM_CODEC, CallbackVibrationListenedPower::hudElement,
		IntProvider.STREAM_CODEC, CallbackVibrationListenedPower::cooldown,
		Action.STREAM_CODEC, CallbackVibrationListenedPower::onListenedAction,
		ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.GAME_EVENT)), CallbackVibrationListenedPower::vibration,
		IntProvider.STREAM_CODEC, CallbackVibrationListenedPower::range,
		Properties.STREAM_CODEC, CallbackVibrationListenedPower::properties,
		CallbackVibrationListenedPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_VIBRATION_LISTENED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		CooldownPower.super.validate(validator);
		onListenedAction().validate(validator.forChild(".on_listened_action"));
		range().validate(validator.forChild(".range"));
	}

	public static final class Instance extends CooldownPower.Instance<CallbackVibrationListenedPower> {

		private Container container;

		Instance(@NotNull CallbackVibrationListenedPower power) {
			super(power);
		}

		@Override
		public void onInit(Entity holder) {

			super.onInit(holder);

			this.reset();
			this.update(holder, DynamicGameEventListener::add);

		}

		@Override
		public void onRevoked(Entity holder) {

			super.onRevoked(holder);

			this.update(holder, DynamicGameEventListener::remove);
			this.reset();

		}

		@Override
		public void onTick(Entity holder) {

			if (holder.level() instanceof ServerLevel serverLevel) {
				this.getOrCreateContainer(holder).system().tick(serverLevel);
			}

		}

		@Override
		public boolean shouldTick(Entity holder) {
			return !holder.level().isClientSide()
				&& this.canListen();
		}

		public boolean doesApply(Context context, Holder<GameEvent> vibration) {
			return this.containsOrEmpty(vibration)
				&& this.canUse(context)
				&& this.isActive(context);
		}

		public int getRange(Entity holder) {
			return power.range().getInt(this.createContext(holder).forChild(".range"));
		}

		public void update(Entity holder, BiConsumer<DynamicGameEventListener<?>, ServerLevel> updater) {

			if (holder.level() instanceof ServerLevel serverLevel) {
				updater.accept(this.getOrCreateContainer(holder).listener(), serverLevel);
			}

		}

		private Container getOrCreateContainer(Entity holder) {

			if (container == null) {
				this.container = Container.of(this, holder);
			}

			return this.container;

		}

		private boolean containsOrEmpty(Holder<GameEvent> vibration) {
			return power().vibration().isEmpty()
				|| power().vibration().get().contains(vibration);
		}

		private boolean canListen() {
			return container != null;
		}

		private void reset() {
			this.container = null;
		}

	}

	public static final class VibrationSubSystem implements VibrationSystem {

		private final CallbackVibrationListenedPower.Instance instance;
		private final Entity holder;

		private final Data data = new Data();
		private final User user = new User();

		private VibrationSubSystem(CallbackVibrationListenedPower.Instance instance, Entity holder) {
			this.instance = instance;
			this.holder = holder;
		}

		@Override
		public @NotNull Data getVibrationData() {
			return data;
		}

		@Override
		public @NotNull User getVibrationUser() {
			return user;
		}

		public void tick(ServerLevel level) {
			VibrationSystem.Ticker.tick(level, this.getVibrationData(), this.getVibrationUser());
		}

		public final class User implements VibrationSystem.User {

			@Override
			public int getListenerRadius() {
				return instance.getRange(holder);
			}

			@Override
			public @NotNull PositionSource getPositionSource() {
				return new EntityPositionSource(holder, holder.getEyeHeight(holder.getPose()));
			}

			@Override
			public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> vibration, GameEvent.Context vibrationContext) {

				Context context = instance.createHolderContextBuilder(holder)
					.withRequired(VIBRATION_POSITION, blockPos.getCenter())
					.withNullable(VIBRATION_EMITTER, vibrationContext.sourceEntity())
					.withRequired(VIBRATION_AFFECTED_BLOCK, CachedBlock.fromLoadedPos(serverLevel, blockPos))
					.build(serverLevel);

				return instance.doesApply(context, vibration);

			}

			@Override
			public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> vibration, @Nullable Entity entity, @Nullable Entity projectileOwner, float distance) {

				Context context = instance.createHolderContextBuilder(holder)
					.withRequired(VIBRATION_POSITION, blockPos.getCenter())
					.withOptional(VIBRATION_AFFECTED_BLOCK, CachedBlock.optionallyFromLoadedPos(serverLevel, blockPos))
					.withNullable(VIBRATION_EMITTER, entity)
					.withNullable(VIBRATION_PROJECTILE_OWNER, projectileOwner)
					.build(serverLevel);

				instance.power().onListenedAction().execute(context.forChild(".on_listened_action"));
				instance.trigger(holder);

			}

		}

	}

	public record Container(VibrationSubSystem system, DynamicGameEventListener<VibrationSystem.Listener> listener) {

		public static Container of(CallbackVibrationListenedPower.Instance instance, Entity holder) {

			var system = new VibrationSubSystem(instance, holder);
			var listener = new DynamicGameEventListener<>(new VibrationSystem.Listener(system));

			return new Container(system, listener);

		}

	}

}
