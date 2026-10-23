package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.action.ActionHolder;
import io.github.eggohito.neo_apoli.action.manager.ActionManager;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record ReferenceAction(ResourceLocation value) implements Action {

	public static final MapCodec<ReferenceAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(ResourceLocation.CODEC.fieldOf("value").forGetter(ReferenceAction::value))
		.apply(instance, ReferenceAction::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ReferenceAction> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, ReferenceAction::value,
		ReferenceAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.REFERENCE;
	}

	@Override
	public void execute(Context context) {

		if (!ActionManager.getInstance().contains(this.value())) {
			return;
		}

		Action action = ActionManager.getInstance().get(this.value()).value();
		var visitor = context.visitor();

		try {

			if (visitor.push(action)) {
				action.execute(context.forChild(".{\"" + this.value() + "\"}"));
			}

			else {
				context.forChild(".value").reportProblem("Action with ID \"" + this.value() + "\" was executed recursively!");
			}

		}

		finally {
			visitor.pop(action);
		}

	}

	@Override
	public void validate(ContextValidator validator) {

		Action.super.validate(validator);

		ResourceKey<Action> actionKey = ResourceKey.create(NeoApoliRegistryKeys.ACTION, this.value());
		ContextValidator valueValidator = validator.forChild(".value");

		if (validator.hasVisited(actionKey)) {
			valueValidator.reportProblem("Action with ID \"" + actionKey.location() + "\" was referenced recursively!");
		}

		else {

			DataResult<Action> actionResult = ActionManager.getInstance()
				.getAsResult(this.value())
				.map(ActionHolder::value);

			actionResult
				.ifSuccess(action -> action.validate(validator.visitChild(".{\"" + actionKey.location() + "\"}", actionKey)))
				.ifError(error -> valueValidator.reportProblem(error.message()));

		}

	}

}
