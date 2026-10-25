package io.github.eggohito.neo_apoli.util;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.EntityContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.box.BoxProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public sealed interface Area extends ContextValidatable {

	EntityContextParameter ENTITY_IN_AREA = new EntityContextParameter(NeoApoli.id("entity_in_area"));

	BlockContextParameter BLOCK_IN_AREA = new BlockContextParameter(NeoApoli.id("block_in_area"));

	Codec<Area> CODEC = Type.CODEC.dispatch(Area::getType, Type::mapCodec);

	StreamCodec<RegistryFriendlyByteBuf, Area> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Area::getType, Type::streamCodec);

	Settings settings();

	Type<?> getType();

	List<Entity> getEntities(Context context);

	List<CachedBlock> getBlocks(Context context);

	default void run(Context context, Action action, Condition condition, int limit) {
		settings().target().run(context, this, action, condition, limit);
	}

	static <A extends Area> Products.P1<RecordCodecBuilder.Mu<A>, Settings> addSettingsField(RecordCodecBuilder.Instance<A> instance) {
		return instance.group(Settings.CODEC.forGetter(Area::settings));
	}

	record RadiusBased(Settings settings, Vec3Provider position, FloatProvider radius) implements Area {

		public static final MapCodec<RadiusBased> CODEC = RecordCodecBuilder.mapCodec(instance ->
			addSettingsField(instance)
				.and(Vec3Provider.CODEC.fieldOf("position").forGetter(RadiusBased::position))
				.and(FloatProvider.CODEC.fieldOf("radius").forGetter(RadiusBased::radius))
				.apply(instance, RadiusBased::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, RadiusBased> STREAM_CODEC = StreamCodec.composite(
			Settings.STREAM_CODEC, RadiusBased::settings,
			Vec3Provider.STREAM_CODEC, RadiusBased::position,
			FloatProvider.STREAM_CODEC, RadiusBased::radius,
			RadiusBased::new
		);

		public static final Type<RadiusBased> TYPE = new Type<>(CODEC, STREAM_CODEC);

		@Override
		public Type<?> getType() {
			return TYPE;
		}

		@Override
		public List<Entity> getEntities(Context context) {
			return unwrap(context, (position, radius) -> settings().shape().getEntitiesWithinRadius(context.level(), position, radius));
		}

		@Override
		public List<CachedBlock> getBlocks(Context context) {
			return unwrap(context, (position, radius) -> settings().shape().getBlocksWithinRadius(context.level(), position, Math.round(radius)));
		}

		@Override
		public void validate(ContextValidator validator) {
			position().validate(validator.forChild(".position"));
			radius().validate(validator.forChild(".radius"));
		}

		private <R> List<R> unwrap(Context context, BiFunction<Vec3, Float, List<R>> applier) {
			return position().getValue(context.forChild(".position"))
				.flatMap(position -> radius().getValue(context.forChild(".radius"))
					.map(radius -> applier.apply(position, radius))).orElse(List.of());
		}

	}

	record BoxBased(Settings settings, BoxProvider box) implements Area {

		public static final MapCodec<BoxBased> CODEC = RecordCodecBuilder.mapCodec(instance ->
			addSettingsField(instance)
				.and(BoxProvider.CODEC.fieldOf("box").forGetter(BoxBased::box))
				.apply(instance, BoxBased::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, BoxBased> STREAM_CODEC = StreamCodec.composite(
			Settings.STREAM_CODEC, BoxBased::settings,
			BoxProvider.STREAM_CODEC, BoxBased::box,
			BoxBased::new
		);

		public static final Type<BoxBased> TYPE = new Type<>(CODEC, STREAM_CODEC);

		@Override
		public Type<?> getType() {
			return TYPE;
		}

		@Override
		public List<Entity> getEntities(Context context) {
			return unwrap(context, box -> settings().shape().getEntitiesWithinBox(context.level(), box));
		}

		@Override
		public List<CachedBlock> getBlocks(Context context) {
			return unwrap(context, box -> settings().shape().getBlocksWithinBox(context.level(), box));
		}

		@Override
		public void validate(ContextValidator validator) {
			box().validate(validator.forChild(".box"));
		}

		private <R> List<R> unwrap(Context context, Function<AABB, List<R>> applier) {
			return box().getValue(context.forChild(".box"))
				.map(applier)
				.orElse(List.of());
		}

	}

	record Type<A extends Area>(MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {

		private static final ImmutableBiMap<String, Type<?>> VALUES = ImmutableBiMap.of(
			"radius", RadiusBased.TYPE,
			"box", BoxBased.TYPE
		);

		public static final Codec<Type<?>> CODEC = CodecUtil.mapped(VALUES);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = StreamCodecUtil.mapped(VALUES);

	}

	record Settings(Target target, Shape shape) {

		public static final MapCodec<Settings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Target.CODEC.fieldOf("target").forGetter(Settings::target),
			Shape.CODEC.fieldOf("shape").forGetter(Settings::shape)
		).apply(instance, Settings::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Settings> STREAM_CODEC = StreamCodec.composite(
			Target.STREAM_CODEC, Settings::target,
			Shape.STREAM_CODEC, Settings::shape,
			Settings::new
		);

	}

	enum Target {

		ENTITY {

			private static final ContextValidator.Parameters PARAMETERS = new ContextValidator.Parameters.Builder()
				.required(ENTITY_IN_AREA)
				.build();

			@Override
			public ContextValidator.Parameters parameters() {
				return PARAMETERS;
			}

			@Override
			public void run(Context context, Area area, Action action, Condition condition, int limit) {

				List<Entity> entities = area.getEntities(context);
				int processed = 0;

				for (var entity : entities) {

					if (limit > 0 && processed >= limit) {
						break;
					}

					processed++;
					Context areaContext = new Context.Builder(context)
						.withRequired(ENTITY_IN_AREA, entity)
						.build(context.level());

					if (condition.test(areaContext.forChild(".area_condition"))) {
						action.execute(areaContext.forChild(".area_action"));
					}

				}

			}

		},

		BLOCK {

			private static final ContextValidator.Parameters PARAMETERS = new ContextValidator.Parameters.Builder()
				.required(BLOCK_IN_AREA)
				.build();

			@Override
			public ContextValidator.Parameters parameters() {
				return PARAMETERS;
			}

			@Override
			public void run(Context context, Area area, Action action, Condition condition, int limit) {

				List<CachedBlock> blocks = area.getBlocks(context);
				int processed = 0;

				for (var block : blocks) {

					if (limit > 0 && processed >= limit) {
						break;
					}

					processed++;
					Context areaContext = new Context.Builder(context)
						.withRequired(BLOCK_IN_AREA, block)
						.build(context.level());

					if (condition.test(areaContext.forChild(".area_condition"))) {
						action.execute(areaContext.forChild(".area_action"));
					}

				}

			}

		};

		public static final Codec<Target> CODEC = CodecUtil.enumType(Target.class);

		public static final StreamCodec<ByteBuf, Target> STREAM_CODEC = StreamCodecUtil.enumType(Target.class);

		public abstract ContextValidator.Parameters parameters();

		public abstract void run(Context context, Area area, Action action, Condition condition, int limit);

	}

}
