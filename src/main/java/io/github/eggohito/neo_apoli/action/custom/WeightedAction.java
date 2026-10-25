package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;

public record WeightedAction(WeightedList<Action> entries) implements Action {

	public static final MapCodec<WeightedAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(WeightedList.nonEmptyCodec(Action.CODEC.fieldOf("action")).fieldOf("entries").forGetter(WeightedAction::entries))
		.apply(instance, WeightedAction::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedAction> STREAM_CODEC = StreamCodec.composite(
		StreamCodecUtil.weightedList(Action.STREAM_CODEC), WeightedAction::entries,
		WeightedAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.WEIGHTED;
	}

	@Override
	public void execute(Context context) {
		entries().neo_apoli$getRandomAndIndex(context.level().getRandom()).ifPresent(pair -> pair.first().execute(context.forChild(".entries[" + pair.secondInt() + "]")));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		MiscUtil.iterateList(entries().unwrap(), (index, weighted) -> weighted.value().validate(validator.forChild(".entries[" + index + "]")));
	}

}
