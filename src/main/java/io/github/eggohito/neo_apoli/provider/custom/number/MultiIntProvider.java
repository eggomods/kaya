package io.github.eggohito.neo_apoli.provider.custom.number;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.util.IntBiFunction;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface MultiIntProvider extends IntProvider {

	List<IntProvider> values();

	@Override
	default void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		ContextValidatable.validate(values(), validator, index -> ".values[" + index + "]");
	}

	default Optional<Integer> iterateAndProcess(Context context, IntBiFunction processor) {

		MutableInt result = new MutableInt();
		MutableBoolean init = new MutableBoolean(false);

		MiscUtil.iterateList(
			values(),
			(index, provider) -> {

				Context valueContext = context.forChild(".values[" + index + "]");
				int value = provider.getInt(valueContext);

				if (!valueContext.hasProblems()) {

					if (init.isTrue()) {
						result.setValue(processor.apply(result.intValue(), value));
					}

					else {
						result.setValue(value);
					}

					init.setTrue();

				}

			}
		);

		return init.isTrue()
			? Optional.of(result.intValue())
			: Optional.empty();

	}

	static <M extends MultiIntProvider> MapCodec<M> mapCodec(Function<List<IntProvider>, M> constructor) {
		return MapCodecUtil.lazy(() -> RecordCodecBuilder.mapCodec(instance -> instance
			.group(IntProvider.CODEC.listOf().fieldOf("values").forGetter(MultiIntProvider::values))
			.apply(instance, constructor)
		));
	}

	static <M extends MultiIntProvider> StreamCodec<RegistryFriendlyByteBuf, M> streamCodec(Function<List<IntProvider>, M> constructor) {
		return StreamCodecUtil.lazy(() -> StreamCodec.composite(
			IntProvider.STREAM_CODEC.apply(ByteBufCodecs.list()), MultiIntProvider::values,
			constructor
		));
	}

}
