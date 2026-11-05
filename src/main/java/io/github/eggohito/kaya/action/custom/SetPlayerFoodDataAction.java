package io.github.eggohito.kaya.action.custom;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.KayaActionTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.OptionalUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodData;

import java.util.Optional;

public record SetPlayerFoodDataAction(Optional<IntProvider> foodLevel, Optional<FloatProvider> saturation, EntityProvider entity) implements Action {

	public static final ContextValidator.Parameters PARAMETERS = new ContextValidator.Parameters.Builder()
		.required(KayaContextParameters.PLAYER_FOOD_LEVEL)
		.required(KayaContextParameters.PLAYER_SATURATION)
		.build();

	private static final MapCodec<SetPlayerFoodDataAction> UNVALIDATED_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.clamped(0, Integer.MAX_VALUE).optionalFieldOf("food_level").forGetter(SetPlayerFoodDataAction::foodLevel),
		FloatProvider.clamped(0, Float.MAX_VALUE).optionalFieldOf("saturation").forGetter(SetPlayerFoodDataAction::saturation),
		EntityProvider.CODEC.fieldOf("entity").forGetter(SetPlayerFoodDataAction::entity)
	).apply(instance, SetPlayerFoodDataAction::new));

	public static final MapCodec<SetPlayerFoodDataAction> CODEC = UNVALIDATED_CODEC.mapResult(new MapCodec.ResultFunction<>() {

		@Override
		public <I> DataResult<SetPlayerFoodDataAction> apply(DynamicOps<I> ops, MapLike<I> input, DataResult<SetPlayerFoodDataAction> result) {
			return result.flatMap(action -> action.validateWithInput(input).map(ignored -> action));
		}

		@Override
		public <O> RecordBuilder<O> coApply(DynamicOps<O> ops, SetPlayerFoodDataAction input, RecordBuilder<O> prefix) {
			return prefix;
		}

	});

	public static final StreamCodec<RegistryFriendlyByteBuf, SetPlayerFoodDataAction> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(IntProvider.STREAM_CODEC), SetPlayerFoodDataAction::foodLevel,
		ByteBufCodecs.optional(FloatProvider.STREAM_CODEC), SetPlayerFoodDataAction::saturation,
		EntityProvider.STREAM_CODEC, SetPlayerFoodDataAction::entity,
		SetPlayerFoodDataAction::new
	);

	@Override
	public Type<?> getType() {
		return KayaActionTypes.SET_PLAYER_FOOD_DATA;
	}

	@Override
	public void execute(Context context) {

		ServerPlayer serverPlayer = entity().getValue(context.forChild(".entity"))
			.flatMap(OptionalUtil.safeCast(ServerPlayer.class))
			.orElse(null);

		if (serverPlayer == null) {
			return;
		}

		FoodData foodData = serverPlayer.getFoodData();
		Context contextWithParams = new Context.Builder(context)
			.withRequired(KayaContextParameters.PLAYER_FOOD_LEVEL, foodData.getFoodLevel())
			.withRequired(KayaContextParameters.PLAYER_SATURATION, foodData.getSaturationLevel())
			.build(serverPlayer.serverLevel());

		foodLevel()
			.flatMap(self -> self.getValue(contextWithParams.forChild(".food_level")))
			.ifPresent(foodData::setFoodLevel);
		saturation()
			.flatMap(self -> self.getValue(contextWithParams.forChild(".saturation")))
			.ifPresent(foodData::setSaturation);

	}

	@Override
	public void validate(ContextValidator validator) {

		Action.super.validate(validator);
		ContextValidator parameterizedValidator = validator.withParams(PARAMETERS);

		this.validate()
			.resultOrPartial(parameterizedValidator::reportProblem)
			.ifPresent(ignored -> {
				foodLevel().ifPresent(foodLevel -> foodLevel.validate(parameterizedValidator.forChild(".food_level")));
				saturation().ifPresent(saturation -> saturation.validate(parameterizedValidator.forChild(".saturation")));
			});

		entity().validate(validator.forChild(".entity"));

	}

	private DataResult<Unit> validate() {

		if (foodLevel().isEmpty() && saturation().isEmpty()) {
			return DataResult.error(() -> "Any of 'food_level' or 'saturation' keys must be present");
		}

		else {
			return DataResult.success(Unit.INSTANCE);
		}

	}

	private <I> DataResult<Unit> validateWithInput(MapLike<I> input) {
		return validate().mapError(error -> error + "in map: " + input);
	}

}
