package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.ListIterator;

public record AnyOfCondition(List<Condition> conditions) implements Condition {

	public static final MapCodec<AnyOfCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Condition.CODEC.listOf().fieldOf("conditions").forGetter(AnyOfCondition::conditions))
		.apply(instance, AnyOfCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, AnyOfCondition> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC.apply(ByteBufCodecs.list()), AnyOfCondition::conditions,
		AnyOfCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ANY_OF;
	}

	@Override
	public boolean test(Context context) {

		ListIterator<Condition> listIterator = conditions().listIterator();

		while (listIterator.hasNext()) {

			Context conditionContext = context.forChild(".conditions[" + listIterator.nextIndex() + "]");
			Condition condition = listIterator.next();

			if (condition.test(conditionContext)) {
				return true;
			}

		}

		return false;

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		ContextValidatable.validate(conditions(), validator, index -> ".conditions[" + index + "]");
	}

}
