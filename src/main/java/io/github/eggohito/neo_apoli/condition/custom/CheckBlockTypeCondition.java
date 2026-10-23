package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.TypeOrTagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public record CheckBlockTypeCondition(TypeOrTagPredicate<Block> predicate, BlockProvider block) implements Condition {

	public static final MapCodec<CheckBlockTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TypeOrTagPredicate.mapCodec(ResourceKey.codec(Registries.BLOCK), TagKey.hashedCodec(Registries.BLOCK)).forGetter(CheckBlockTypeCondition::predicate),
		BlockProvider.CODEC.fieldOf("block").forGetter(CheckBlockTypeCondition::block)
	).apply(instance, CheckBlockTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CheckBlockTypeCondition> STREAM_CODEC = StreamCodec.composite(
		TypeOrTagPredicate.streamCodec(ResourceKey.streamCodec(Registries.BLOCK), TagKey.streamCodec(Registries.BLOCK)), CheckBlockTypeCondition::predicate,
		BlockProvider.STREAM_CODEC, CheckBlockTypeCondition::block,
		CheckBlockTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.CHECK_BLOCK_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return block().getValue(context.forChild(".block"))
			.map(CachedBlock::state)
			.filter(state -> predicate().test(state::is, state::is))
			.isPresent();
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		predicate().validate(validator);
		block().validate(validator.forChild(".block"));
	}

}
