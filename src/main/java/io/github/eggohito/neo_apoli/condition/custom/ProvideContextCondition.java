package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextParameterMap;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ProvideContextCondition(Condition condition, ContextParameterMap parameters) implements Condition {

	public static final MapCodec<ProvideContextCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(ProvideContextCondition::condition),
		ContextParameterMap.CODEC.fieldOf("parameters").forGetter(ProvideContextCondition::parameters)
	).apply(instance, ProvideContextCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ProvideContextCondition> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, ProvideContextCondition::condition,
		ContextParameterMap.STREAM_CODEC, ProvideContextCondition::parameters,
		ProvideContextCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.PROVIDE_CONTEXT;
	}

	@Override
	public boolean test(Context context) {
		return condition().test(parameters()
			.forUser(context, ctx -> ctx.forChild(".parameters"))
			.forChild(".condition")
		);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		condition().validate(validator.withParams(parameters().forValidation()).forChild(".condition"));
		parameters().validate(validator.forChild(".parameters"));
	}

}
