package io.github.eggohito.neo_apoli.hud.element;

import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.FloatContextParameter;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;

import java.util.Optional;

public interface NumberBoundHudElement extends HudElement {

	FloatContextParameter CURRENT_VALUE = new FloatContextParameter(NeoApoli.id("hud/value"));

	FloatContextParameter MAX_VALUE = new FloatContextParameter(NeoApoli.id("hud/max_value"));

	FloatContextParameter MIN_VALUE = new FloatContextParameter(NeoApoli.id("hud/min_value"));

	Optional<FloatProvider> value();

	Optional<FloatProvider> min();

	Optional<FloatProvider> max();

	@Override
	default void validate(ContextValidator validator) {

		HudElement.super.validate(validator);

		validateKeyAndField(validator, CURRENT_VALUE, value(), "value");
		validateKeyAndField(validator, MAX_VALUE, max(), "max");
		validateKeyAndField(validator, MIN_VALUE, min(), "min");

	}

	static <P extends ValueProvider<?>> void validateKeyAndField(ContextValidator validator, ContextParameter<?> key, Optional<P> fieldMethod, String fieldName) {

		boolean keyIsAllowed = validator.parameters().allowed().contains(key);
		boolean fieldIsPresent = fieldMethod.isPresent();

		if (keyIsAllowed == fieldIsPresent) {
			validator.reportProblem("Either the parameter \"" + key.name() + "\" must be provided or the field \"" + fieldName + "\" be defined" + (fieldIsPresent ? ", not both" : "") + "!");
		}

	}

}
