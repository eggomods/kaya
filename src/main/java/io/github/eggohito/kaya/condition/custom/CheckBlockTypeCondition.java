package io.github.eggohito.kaya.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.condition.Condition;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.block.BlockProvider;
import io.github.eggohito.kaya.registry.KayaConditionTypes;
import io.github.eggohito.kaya.util.CachedBlock;
import io.github.eggohito.kaya.util.EitherHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

public record CheckBlockTypeCondition(EitherHolder<Block> predicate, BlockProvider block) implements Condition {

	public static final MapCodec<CheckBlockTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EitherHolder.mapCodec(Registries.BLOCK).forGetter(CheckBlockTypeCondition::predicate),
		BlockProvider.CODEC.fieldOf("block").forGetter(CheckBlockTypeCondition::block)
	).apply(instance, CheckBlockTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckBlockTypeCondition> STREAM_CODEC = StreamCodec.composite(
		EitherHolder.streamCodec(Registries.BLOCK), CheckBlockTypeCondition::predicate,
		BlockProvider.STREAM_CODEC, CheckBlockTypeCondition::block,
		CheckBlockTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return KayaConditionTypes.CHECK_BLOCK_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return block().getValue(context.forChild(".block"))
			.stream()
			.map(CachedBlock::state)
			.anyMatch(state -> predicate().keyOrTag().map(state::is, state::is));
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		block().validate(validator.forChild(".block"));
	}

}
