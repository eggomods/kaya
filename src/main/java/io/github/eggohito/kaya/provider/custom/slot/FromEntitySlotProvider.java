package io.github.eggohito.kaya.provider.custom.slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.provider.KayaSlotProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FromEntitySlotProvider(EntityProvider entity, IntProvider slot) implements SlotProvider {

	public static final MapCodec<FromEntitySlotProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("entity").forGetter(FromEntitySlotProvider::entity),
		IntProvider.CODEC.fieldOf("slot").forGetter(FromEntitySlotProvider::slot)
	).apply(instance, FromEntitySlotProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FromEntitySlotProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, FromEntitySlotProvider::entity,
		IntProvider.STREAM_CODEC, FromEntitySlotProvider::slot,
		FromEntitySlotProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaSlotProviderTypes.FROM_ENTITY;
	}

	@Override
	public Optional<SlotAccess> getValue(Context context) {

		Context slotContext = context.forChild(".slot");
		int slot = slot().getInt(slotContext);

		if (slotContext.hasProblems()) {
			return Optional.empty();
		}

		else {
			return entity()
				.getValue(context.forChild(".entity"))
				.map(entity -> entity.getSlot(slot));
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		SlotProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
		slot().validate(validator.forChild(".slot"));
	}

}
