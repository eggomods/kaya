package io.github.eggohito.neo_apoli.util;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public sealed interface Area extends ContextValidatable {

	EntityContextParameter ENTITY_IN_AREA = new EntityContextParameter(NeoApoli.id("entity_in_area"));

	BlockContextParameter BLOCK_IN_AREA = new BlockContextParameter(NeoApoli.id("block_in_area"));

	Codec<Area> CODEC = Type.CODEC.dispatch(Area::getType, Type::mapCodec);

	StreamCodec<RegistryFriendlyByteBuf, Area> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Area::getType, Type::streamCodec);

	Settings settings();

	Type<?> getType();

	Iterable<Entity> getEntities(Context context);

	Iterable<CachedBlock> getBlocks(Context context);

	default ContextValidator.Parameters parameters() {
		return settings().target().parameters();
	}

	default void run(Context context, Consumer<Context> onMatch, Predicate<Context> matchFilter, int limit) {
		settings().target().run(context, this, onMatch, matchFilter, limit);
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
		public Iterable<Entity> getEntities(Context context) {
			return unwrap(context, (position, radius) -> settings().shape().iterateEntitiesWithinRadius(context.level(), position, radius));
		}

		@Override
		public Iterable<CachedBlock> getBlocks(Context context) {
			return unwrap(context, (position, radius) -> settings().shape().iterateBlocksWithinRadius(context.level(), position, Math.round(radius)));
		}

		@Override
		public void validate(ContextValidator validator) {
			position().validate(validator.forChild(".position"));
			radius().validate(validator.forChild(".radius"));
		}

		private <R> Iterable<R> unwrap(Context context, BiFunction<Vec3, Float, Iterable<R>> applier) {
			return position().getValue(context.forChild(".position"))
				.flatMap(position -> radius().getValue(context.forChild(".radius"))
					.map(radius -> applier.apply(position, radius))).orElse(MiscUtil.emptyIterable());
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
		public Iterable<Entity> getEntities(Context context) {
			return unwrap(context, box -> settings().shape().iterateEntitiesWithinBox(context.level(), box));
		}

		@Override
		public Iterable<CachedBlock> getBlocks(Context context) {
			return unwrap(context, box -> settings().shape().iterateBlocksWithinBox(context.level(), box));
		}

		@Override
		public void validate(ContextValidator validator) {
			box().validate(validator.forChild(".box"));
		}

		private <R> Iterable<R> unwrap(Context context, Function<AABB, Iterable<R>> applier) {
			return box().getValue(context.forChild(".box"))
				.map(applier)
				.orElse(MiscUtil.emptyIterable());
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
			public void run(Context context, Area area, Consumer<Context> onMatch, Predicate<Context> matchFilter, int limit) {

				Iterable<Entity> entities = area.getEntities(context);
				int processed = 0;

				for (var entity : entities) {

					Context areaContext = new Context.Builder(context)
						.withRequired(ENTITY_IN_AREA, entity)
						.build(context.level());

					if (matchFilter.test(areaContext)) {
						onMatch.accept(areaContext);
						processed++;
					}

					if (limit >= 0 && processed >= limit) {
						break;
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
			public void run(Context context, Area area, Consumer<Context> onMatch, Predicate<Context> matchFilter, int limit) {

				Iterable<CachedBlock> blocks = area.getBlocks(context);
				int processed = 0;

				for (var block : blocks) {

					Context areaContext = new Context.Builder(context)
						.withRequired(BLOCK_IN_AREA, block)
						.build(context.level());

					if (matchFilter.test(areaContext)) {
						onMatch.accept(areaContext);
						processed++;
					}

					if (limit >= 0 && processed >= limit) {
						break;
					}

				}

			}

		};

		public static final Codec<Target> CODEC = CodecUtil.enumType(Target.class);

		public static final StreamCodec<ByteBuf, Target> STREAM_CODEC = StreamCodecUtil.enumType(Target.class);

		public abstract ContextValidator.Parameters parameters();

		public abstract void run(Context context, Area area, Consumer<Context> onMatch, Predicate<Context> matchFilter, int limit);

	}

}
