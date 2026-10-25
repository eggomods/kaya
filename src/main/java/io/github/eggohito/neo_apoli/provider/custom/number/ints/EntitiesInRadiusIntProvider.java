package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.EntityContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.Shape;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record EntitiesInRadiusIntProvider(Condition condition, Vec3Provider position, Shape shape, FloatProvider radius) implements IntProvider {

	public static final EntityContextParameter ENTITY_IN_RADIUS = new EntityContextParameter(NeoApoli.id("entity_in_radius"));
	public static final ContextValidator.Parameters CONDITION_PARAMETER_SET = new ContextValidator.Parameters.Builder().required(ENTITY_IN_RADIUS).build();

	public static final MapCodec<EntitiesInRadiusIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(EntitiesInRadiusIntProvider::condition),
		Vec3Provider.CODEC.fieldOf("position").forGetter(EntitiesInRadiusIntProvider::position),
		Shape.CODEC.fieldOf("shape").forGetter(EntitiesInRadiusIntProvider::shape),
		FloatProvider.CODEC.fieldOf("radius").forGetter(EntitiesInRadiusIntProvider::radius)
	).apply(instance, EntitiesInRadiusIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntitiesInRadiusIntProvider> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, EntitiesInRadiusIntProvider::condition,
		Vec3Provider.STREAM_CODEC, EntitiesInRadiusIntProvider::position,
		Shape.STREAM_CODEC, EntitiesInRadiusIntProvider::shape,
		FloatProvider.STREAM_CODEC, EntitiesInRadiusIntProvider::radius,
		EntitiesInRadiusIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.ENTITIES_IN_RADIUS;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Vec3 position = position()
			.getValue(context.forChild(".position"))
			.orElse(null);

		if (position == null) {
			return Optional.empty();
		}

		Level level = context.level();
		int matches = 0;

		float radius = radius().getFloat(context.forChild(".radius"));
		List<Entity> targets = shape().getEntitiesWithinRadius(level, position, radius);

		for (var target : targets) {

			Context entityContext = new Context.Builder(context)
				.withRequired(ENTITY_IN_RADIUS, target)
				.build(level);

			if (condition().test(entityContext.forChild(".condition"))) {
				matches++;
			}

		}

		return Optional.of(matches);

	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		condition().validate(validator.withParams(CONDITION_PARAMETER_SET).forChild(".condition"));
		position().validate(validator.forChild(".position"));
		radius().validate(validator.forChild(".radius"));
	}

}
