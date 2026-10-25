package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record BlockNbtProvider(BlockProvider block) implements NbtProvider {

	public static final MapCodec<BlockNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(BlockProvider.CODEC.fieldOf("block").forGetter(BlockNbtProvider::block))
		.apply(instance, BlockNbtProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockNbtProvider> STREAM_CODEC = StreamCodec.composite(
		BlockProvider.STREAM_CODEC, BlockNbtProvider::block,
		BlockNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.BLOCK;
	}

	@Override
	public Optional<Tag> getValue(Context context) {
		return block()
			.getValue(context.forChild(".block"))
			.map(block -> this.serialize(context, block));
	}

	@Override
	public void validate(ContextValidator validator) {
		NbtProvider.super.validate(validator);
		block().validate(validator.forChild(".block"));
	}

	private CompoundTag serialize(Context context, CachedBlock block) {

		RegistryAccess registryAccess = context.level().registryAccess();
		RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);

		CompoundTag compoundTag = new CompoundTag();

		BlockPos.CODEC.encodeStart(ops, block.pos())
			.result()
			.ifPresent(posTag -> compoundTag.put("pos", posTag));
		BlockState.CODEC.encodeStart(ops, block.state())
			.result()
			.ifPresent(stateTag -> compoundTag.put("state", stateTag));
		Optional.ofNullable(block.entity())
			.map(entity -> entity.saveWithId(registryAccess))
			.ifPresent(entityTag -> compoundTag.put("entity", entityTag));

		return compoundTag;

	}

}
