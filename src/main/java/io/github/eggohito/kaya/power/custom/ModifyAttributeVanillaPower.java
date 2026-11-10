package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.AttributeModifyingPower;
import io.github.eggohito.kaya.provider.custom.bool.BooleanProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.util.AttributedModifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ModifyAttributeVanillaPower(List<AttributedModifier> modifiers, BooleanProvider sendUpdate, IntProvider tickRate, Properties properties) implements AttributeModifyingPower {

	public static final MapCodec<ModifyAttributeVanillaPower> CODEC = RecordCodecBuilder.mapCodec(instance -> AttributeModifyingPower
		.addFields(instance)
		.and(IntProvider.clamped(1, Integer.MAX_VALUE).optionalFieldOf("tick_rate", new ConstantIntProvider(20)).forGetter(ModifyAttributeVanillaPower::tickRate))
		.and(Power.propertiesCodecBuilder(true))
		.apply(instance, ModifyAttributeVanillaPower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyAttributeVanillaPower> STREAM_CODEC = StreamCodec.composite(
		KayaStreamCodecs.ATTRIBUTE_MODIFIERS, AttributeModifyingPower::modifiers,
		BooleanProvider.STREAM_CODEC, AttributeModifyingPower::sendUpdate,
		IntProvider.STREAM_CODEC, ModifyAttributeVanillaPower::tickRate,
		Properties.STREAM_CODEC, ModifyAttributeVanillaPower::properties,
		ModifyAttributeVanillaPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_ATTRIBUTE_VANILLA;
	}

	@Override
	public AttributeModifyingPower.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends AttributeModifyingPower.Instance<ModifyAttributeVanillaPower> {

		private Integer startTicks;
		private Integer endTicks;

		private boolean wasActive;

		protected Instance(@NotNull ModifyAttributeVanillaPower power) {
			super(power);
		}

		@Override
		public void onGranted(Entity holder) {

			super.onGranted(holder);

			if (!this.shouldTick(holder)) {
				this.addModifiersPersistently(holder, this.createHolderContext(holder));
			}

		}

		@Override
		public void onRespawned(Entity holder) {

			super.onRespawned(holder);

			if (!this.shouldTick(holder)) {
				this.addModifiersPersistently(holder, this.createHolderContext(holder));
			}

		}

		@Override
		public void onRevoked(Entity holder) {
			super.onRevoked(holder);
			this.removeModifiers(holder, this.createHolderContext(holder));
		}

		@Override
		public void onTick(Entity holder) {

			Context context = createHolderContext(holder);
			int tickRate = power.tickRate().getInt(context.forChild(".tick_rate"));

			if (context.hasProblems()) {

				this.startTicks = null;
				this.endTicks = null;

				this.wasActive = false;

			}

			else {

				int ticks = holder.tickCount % tickRate;
				if (this.isActive(context)) {

					if (startTicks == null) {
						this.startTicks = ticks;
						this.endTicks = null;
					}

					else if (!wasActive && ticks == startTicks) {
						this.addModifiersTemporarily(holder, context);
						this.wasActive = true;
					}

				}

				else if (wasActive) {

					if (endTicks == null) {
						this.startTicks = null;
						this.endTicks = ticks;
					}

					else if (ticks == endTicks) {
						this.removeModifiers(holder, context);
						this.wasActive = false;
					}

				}

			}

		}

		@Override
		public boolean shouldTick(Entity holder) {
			return power.activeCondition().isPresent();
		}

	}

}
