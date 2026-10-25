package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.comparison.Comparator;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public record BlockStatePropertyCondition(StringProvider property, BlockProvider block, StringProvider value) implements Condition {

	public static final MapCodec<BlockStatePropertyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		StringProvider.CODEC.fieldOf("property").forGetter(BlockStatePropertyCondition::property),
		BlockProvider.CODEC.fieldOf("block").forGetter(BlockStatePropertyCondition::block),
		StringProvider.CODEC.fieldOf("value").forGetter(BlockStatePropertyCondition::value)
	).apply(instance, BlockStatePropertyCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockStatePropertyCondition> STREAM_CODEC = StreamCodec.composite(
		StringProvider.STREAM_CODEC, BlockStatePropertyCondition::property,
		BlockProvider.STREAM_CODEC, BlockStatePropertyCondition::block,
		StringProvider.STREAM_CODEC, BlockStatePropertyCondition::value,
		BlockStatePropertyCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.BLOCK_STATE_PROPERTY;
	}

	@Override
	public boolean test(Context context) {

		String propertyName = property()
			.getValue(context.forChild(".property"))
			.orElse(null);

		if (propertyName == null) {
			return false;
		}

		CachedBlock block = block()
			.getValue(context.forChild(".block"))
			.orElse(null);

		if (block == null) {
			return false;
		}

		BlockState state = block.state();
		Property<?> property = state.getBlock().getStateDefinition().getProperty(propertyName);

		if (property == null) {
			context.reportProblem("Block \"" + Util.getRegisteredName(BuiltInRegistries.BLOCK, state.getBlock()) + "\" didn't have the \"" + propertyName + "\" state property!");
		}

		return property != null
			&& this.testProperty(context, state, property);

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		property().validate(validator.forChild(".property"));
		block().validate(validator.forChild(".block"));
		value().validate(validator.forChild(".value"));
	}

	private <T extends Comparable<T>> boolean testProperty(Context context, BlockState state, Property<T> property) {
		return value().getValue(context.forChild(".value"))
			.flatMap(property::getValue)
			.filter(value -> Comparator.EQUALS.compare(state.getValue(property), value))
			.isPresent();
	}

}
