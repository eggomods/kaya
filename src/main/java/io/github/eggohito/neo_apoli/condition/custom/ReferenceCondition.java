package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.manager.ConditionManager;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record ReferenceCondition(ResourceLocation value) implements Condition {

	public static final MapCodec<ReferenceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(ResourceLocation.CODEC.fieldOf("value").forGetter(ReferenceCondition::value))
		.apply(instance, ReferenceCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ReferenceCondition> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, ReferenceCondition::value,
		ReferenceCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.REFERENCE;
	}

	@Override
	public boolean test(Context context) {
		return ConditionManager.getInstance().getAsResult(this.value()).mapOrElse(
			condition -> {

				try {

					if (context.visitor().push(condition)) {
						return condition.test(context.forChild(".{\"" + this.value() + "\"}"));
					}

					else {
						context.forChild(".value").reportProblem("Condition with ID \"" + this.value() + "\" was tested recursively!");
					}

				}

				finally {
					context.visitor().pop(condition);
				}

				return false;

			},
			error -> false
		);
	}

	@Override
	public void validate(ContextValidator validator) {

		Condition.super.validate(validator);

		ResourceKey<Condition> valueKey = ResourceKey.create(NeoApoliRegistryKeys.CONDITION, this.value());
		ContextValidator valueValidator = validator.forChild(".value");

		if (validator.hasVisited(valueKey)) {
			valueValidator.reportProblem("Condition with ID \"" + valueKey.location() + "\" was referenced recursively!");
		}

		else {
			ConditionManager.getInstance().getAsResult(this.value())
				.ifSuccess(condition -> condition.validate(validator.visitChild(".{\"" + valueKey.location() + "\"}", valueKey)))
				.ifError(error -> valueValidator.reportProblem(error.message()));
		}

	}

}
