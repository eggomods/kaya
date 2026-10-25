package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.area.Area;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record CountInAreaIntProvider(Area area, Condition condition, IntProvider limit) implements IntProvider {

	public static final MapCodec<CountInAreaIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Area.CODEC.fieldOf("area").forGetter(CountInAreaIntProvider::area),
		Condition.CODEC.optionalFieldOf("condition", new ConstantCondition(true)).forGetter(CountInAreaIntProvider::condition),
		IntProvider.CODEC.optionalFieldOf("limit", new ConstantIntProvider(-1)).forGetter(CountInAreaIntProvider::limit)
	).apply(instance, CountInAreaIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CountInAreaIntProvider> STREAM_CODEC = StreamCodec.composite(
		Area.STREAM_CODEC, CountInAreaIntProvider::area,
		Condition.STREAM_CODEC, CountInAreaIntProvider::condition,
		IntProvider.STREAM_CODEC, CountInAreaIntProvider::limit,
		CountInAreaIntProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliIntProviderTypes.COUNT_IN_AREA;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		MutableInt result = new MutableInt();
		int oldValue = result.intValue();

		limit().getValue(context.forChild(".limit")).ifPresent(limit -> run(context, result, limit));
		int newValue = result.intValue();

		return oldValue != newValue
			? Optional.of(newValue)
			: Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		area().validate(validator.forChild(".area"));
		condition().validate(validator.withParams(area().parameters()).forChild(".condition"));
		limit().validate(validator.forChild(".limit"));
	}

	private void run(Context context, MutableInt result, int limit) {
		area().run(context, ignored -> result.increment(), ctx -> condition().test(ctx.forChild(".condition")), limit);
	}

}
