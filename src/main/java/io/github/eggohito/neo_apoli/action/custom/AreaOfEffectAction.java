package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.area.Area;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record AreaOfEffectAction(Area area, Action action, Condition condition, IntProvider limit) implements Action {

	public static final MapCodec<AreaOfEffectAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Area.CODEC.fieldOf("area").forGetter(AreaOfEffectAction::area),
		Action.CODEC.fieldOf("action").forGetter(AreaOfEffectAction::action),
		Condition.CODEC.optionalFieldOf("condition", new ConstantCondition(true)).forGetter(AreaOfEffectAction::condition),
		IntProvider.CODEC.optionalFieldOf("limit", new ConstantIntProvider(-1)).forGetter(AreaOfEffectAction::limit)
	).apply(instance, AreaOfEffectAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AreaOfEffectAction> STREAM_CODEC = StreamCodec.composite(
		Area.STREAM_CODEC, AreaOfEffectAction::area,
		Action.STREAM_CODEC, AreaOfEffectAction::action,
		Condition.STREAM_CODEC, AreaOfEffectAction::condition,
		IntProvider.STREAM_CODEC, AreaOfEffectAction::limit,
		AreaOfEffectAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.AREA_OF_EFFECT;
	}

	@Override
	public void execute(Context context) {
		limit().getValue(context.forChild(".limit")).ifPresent(limit -> run(context, limit));
	}

	@Override
	public void validate(ContextValidator validator) {

		Action.super.validate(validator);
		ContextValidator parameterizedValidator = validator.withParams(area().parameters());

		area().validate(validator);
		action().validate(parameterizedValidator.forChild(".action"));
		condition().validate(parameterizedValidator.forChild(".condition"));
		limit().validate(validator.forChild(".limit"));

	}

	private void run(Context context, int limit) {
		area().run(context, ctx -> action().execute(ctx.forChild(".action")), ctx -> condition().test(ctx.forChild(".condition")), limit);
	}

}
