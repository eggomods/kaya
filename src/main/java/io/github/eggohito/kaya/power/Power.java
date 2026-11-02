package io.github.eggohito.kaya.power;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.parameter.ContextParameter;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundUpdatePowerDataPacket;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.Reporter;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * 	<p>A power gives a certain "ability" to an entity upon being granted. As for what kind of "ability" it provides will
 * 	depend on the implementation (see: {@link Instance}) of the power itself.</p>
 */
public interface Power extends ContextUser {

	String TYPE_KEY = "type";

	MapCodec<Power> MAP_CODEC = Type.CODEC.dispatchMap(TYPE_KEY, Power::getType, Type::mapCodec);

	Codec<Power> CODEC = MAP_CODEC.codec();

	StreamCodec<RegistryFriendlyByteBuf, Power> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Power::getType, Type::streamCodec);

	Type<?> getType();

	Instance<?> createInstance();

	default Optional<Condition> activeCondition() {
		return Optional.empty();
	}

	default boolean canBePartiallyParsed() {
		return false;
	}

	@Override
	default void validate(ContextValidator validator) {
		this.activeCondition().ifPresent(activeCondition -> activeCondition.validate(validator.forChild(".active_condition")));
	}

	static <P extends Power> Products.P1<RecordCodecBuilder.Mu<P>, Optional<Condition>> addActiveConditionField(RecordCodecBuilder.Instance<P> instance) {
		return instance.group(Condition.CODEC.optionalFieldOf("active_condition").forGetter(Power::activeCondition));
	}

	/**
	 * 	<p>The class is responsible for providing the functionality of a power. An instance of this class is created
	 * 	every time a power is granted to an entity to ensure that each instance is unique to each entity.</p>
	 *
	 * 	<p>The uniqueness of each instance is especially relevant for storing data.</p>
	 */
	@Accessors(fluent = true)
	@Getter
	abstract class Instance<P extends Power> implements ContextUser {

		protected final PowerIdentifier id;
		protected final P power;

		protected Instance(@NotNull P power) {
			this.id = PowerManager.getInstance().getKeyAsResult(power).getOrThrow(error -> new IllegalStateException("Tried to created an instance of an unregistered power!"));
			this.power = power;
		}

		@Override
		public Set<ContextParameter<?>> getRequiredParameters() {
			return power.getRequiredParameters();
		}

		@Override
		public void validate(ContextValidator validator) {
			power.validate(validator);
		}

		public Context.Builder createHolderContextBuilder(Entity holder) {
			return new Context.Builder()
				.withReporter(new Reporter("{\"" + this.id() + "\"}"))
				.withRequired(KayaContextParameters.THIS_ENTITY, holder);
		}

		public Context createHolderContext(Entity holder) {
			return this.createHolderContextBuilder(holder).build(holder.level());
		}

		public final void syncData(Entity holder) {

			if (!Powers.has(holder) || (holder instanceof LivingEntity living && living.isDeadOrDying())) {
				return;
			}

			Level level = holder.level();
			RegistryOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);

			if (!PowerManager.getInstance().contains(id)) {
				Kaya.LOGGER.warn("Couldn't sync data of unregistered {} from entity {}!", id.asDisplayString(false), holder.getName().getString());
			}

			else if (level.isClientSide()) {
				Kaya.LOGGER.warn("Couldn't sync data of {} from entity {} in the client!", id.asDisplayString(false), holder.getName().getString());
			}

			else {
				MiscUtil.handleResult(
					this.encodeData(ops),
					tag -> MiscUtil.broadcastCustomToAll(holder, () -> new ClientboundUpdatePowerDataPacket(holder.getId(), id, tag)),
					warning -> Kaya.LOGGER.warn("Found warnings while encoding data of instance for {} on entity {}: {}", id.asDisplayString(false), holder.getName().getString(), warning),
					error -> Kaya.LOGGER.error("Couldn't encode and send data of instance for {} on entity {} (skipping): {}", id.asDisplayString(false), holder.getName().getString(), error)
				);
			}

		}

		public <O> RecordBuilder<O> encodeData(DynamicOps<O> ops, RecordBuilder<O> prefix) {
			return prefix;
		}

		public final <O> DataResult<O> encodeData(DynamicOps<O> ops) {
			return this.encodeData(ops, ops.mapBuilder()).build(ops.empty());
		}

		public <I> DataResult<Unit> decodeData(DynamicOps<I> ops, MapLike<I> mapInput) {
			return DataResult.success(Unit.INSTANCE);
		}

		public final <I> DataResult<Unit> decodeData(DynamicOps<I> ops, I input) {
			return ops.getMap(input).flatMap(mapInput -> this.decodeData(ops, mapInput));
		}

		public void onAdded(Entity holder) {

		}

		public void onGranted(Entity holder) {

		}

		public void onRemoved(Entity holder) {

		}

		public void onRevoked(Entity holder) {

		}

		public void onRespawned(Entity holder) {

		}

		public void onTick(Entity holder) {

		}

		public void onInit(Entity holder) {

		}

		public boolean isImmutable(Entity holder) {
			return true;
		}

		public boolean shouldTick(Entity holder) {
			return false;
		}

		public boolean isActive(Context context) {
			return power.activeCondition()
				.map(activeCondition -> activeCondition.test(context.forChild(".active_condition")))
				.orElse(true);
		}

	}

	record Type<P extends Power>(ContextValidator.Parameters parameters, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.POWER_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

}
