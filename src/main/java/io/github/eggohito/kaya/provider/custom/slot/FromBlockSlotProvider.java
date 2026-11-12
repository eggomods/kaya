package io.github.eggohito.kaya.provider.custom.slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.block.BlockProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.provider.KayaSlotProviderTypes;
import io.github.eggohito.kaya.util.CachedBlock;
import io.github.eggohito.kaya.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FromBlockSlotProvider(BlockProvider block, IntProvider slot) implements SlotProvider {

	public static final MapCodec<FromBlockSlotProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockProvider.CODEC.fieldOf("block").forGetter(FromBlockSlotProvider::block),
		IntProvider.CODEC.fieldOf("slot").forGetter(FromBlockSlotProvider::slot)
	).apply(instance, FromBlockSlotProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FromBlockSlotProvider> STREAM_CODEC = StreamCodec.composite(
		BlockProvider.STREAM_CODEC, FromBlockSlotProvider::block,
		IntProvider.STREAM_CODEC, FromBlockSlotProvider::slot,
		FromBlockSlotProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaSlotProviderTypes.FROM_BLOCK;
	}

	@Override
	public Optional<SlotAccess> getValue(Context context) {

		Context slotContext = context.forChild(".slot");
		int slot = slot().getInt(slotContext);

		if (slotContext.hasProblems()) {
			return Optional.empty();
		}

		else {

			Context blockContext = context.forChild(".block");
			Optional<CachedBlock> block = block().getValue(blockContext);

			Optional<Container> container = block
				.flatMap(self -> Optional.ofNullable(self.entity()))
				.filter(Container.class::isInstance)
				.map(Container.class::cast);

			if (block.isPresent() && container.isEmpty()) {
				blockContext.reportProblem("Block is not a container!");
			}

			return container.flatMap(self -> MiscUtil.createContainerSlotSafely(self, slot));

		}

	}

	@Override
	public void validate(ContextValidator validator) {
		SlotProvider.super.validate(validator);
		block().validate(validator.forChild(".block"));
		slot().validate(validator.forChild(".slot"));
	}

}
