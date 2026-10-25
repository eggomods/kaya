package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Set;

public record ParameterExistsCondition(ContextParameter<?> parameter) implements Condition {

	public static final MapCodec<ParameterExistsCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(ContextParameter.CODEC.fieldOf("parameter").forGetter(ParameterExistsCondition::parameter))
		.apply(instance, ParameterExistsCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ParameterExistsCondition> STREAM_CODEC = StreamCodec.composite(
		ContextParameter.STREAM_CODEC, ParameterExistsCondition::parameter,
		ParameterExistsCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.PARAMETER_EXISTS;
	}

	@Override
	public boolean test(Context context) {
		return context.hasParameter(parameter());
	}

	@Override
	public Set<ContextParameter<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}
