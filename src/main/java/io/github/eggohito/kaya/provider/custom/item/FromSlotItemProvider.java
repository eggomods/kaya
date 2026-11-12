package io.github.eggohito.kaya.provider.custom.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.slot.SlotProvider;
import io.github.eggohito.kaya.registry.provider.KayaItemProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FromSlotItemProvider(SlotProvider slot) implements ItemProvider {

	public static final MapCodec<FromSlotItemProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(SlotProvider.CODEC.fieldOf("slot").forGetter(FromSlotItemProvider::slot))
		.apply(instance, FromSlotItemProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, FromSlotItemProvider> STREAM_CODEC = StreamCodec.composite(
		SlotProvider.STREAM_CODEC, FromSlotItemProvider::slot,
		FromSlotItemProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaItemProviderTypes.FROM_SLOT;
	}

	@Override
	public Optional<ItemStack> getValue(Context context) {
		return slot().getValue(context.forChild(".slot")).map(SlotAccess::get);
	}

	@Override
	public void validate(ContextValidator validator) {
		ItemProvider.super.validate(validator);
		slot().validate(validator.forChild(".slot"));
	}

}
