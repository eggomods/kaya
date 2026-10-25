package io.github.eggohito.neo_apoli.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;

import java.util.Optional;
import java.util.function.Function;

public interface WeightedValueProvider<Value, Provider extends ValueProvider<Value>> extends ValueProvider<Value> {

	WeightedList<Provider> entries();

	@Override
	default Optional<Value> getValue(Context context) {
		return entries()
			.neo_apoli$getRandomAndIndex(context.level().getRandom())
			.flatMap(pair -> pair.first().getValue(context.forChild(".entries[" + pair.secondInt() + "]")));
	}

	@Override
	default void validate(ContextValidator validator) {
		ValueProvider.super.validate(validator);
		MiscUtil.iterateList(entries().unwrap(), (index, entry) -> entry.value().validate(validator.forChild(".entries[" + index + "]")));
	}

	static <P extends ValueProvider<?>, M extends WeightedValueProvider<?, P>> MapCodec<M> mapCodec(MapCodec<P> providerCodec, Function<WeightedList<P>, M> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance
			.group(WeightedList.nonEmptyCodec(providerCodec).fieldOf("entries").forGetter(WeightedValueProvider::entries))
			.apply(instance, constructor)
		);
	}

	static <P extends ValueProvider<?>, M extends WeightedValueProvider<?, P>> MapCodec<M> mapCodec(Codec<P> providerCodec, Function<WeightedList<P>, M> constructor) {
		return mapCodec(providerCodec.fieldOf("value"), constructor);
	}

	static <P extends ValueProvider<?>, M extends WeightedValueProvider<?, P>> StreamCodec<RegistryFriendlyByteBuf, M> streamCodec(StreamCodec<RegistryFriendlyByteBuf, P> streamCodec, Function<WeightedList<P>, M> constructor) {
		return StreamCodec.composite(
			StreamCodecUtil.weightedList(streamCodec), WeightedValueProvider::entries,
			constructor
		);
	}

}
