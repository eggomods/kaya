package io.github.eggohito.neo_apoli.area;

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
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.github.eggohito.neo_apoli.util.Shape;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Area extends ContextValidatable {

	EntityContextParameter ENTITY_IN_AREA = new EntityContextParameter(NeoApoli.id("entity_in_area"));

	BlockContextParameter BLOCK_IN_AREA = new BlockContextParameter(NeoApoli.id("block_in_area"));

	Codec<Area> CODEC = Type.CODEC.dispatch(Area::getType, Type::mapCodec);

	StreamCodec<RegistryFriendlyByteBuf, Area> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Area::getType, Type::streamCodec);

	Type<?> getType();

	Settings settings();

	Iterable<Entity> iterateEntities(Context context);

	Iterable<CachedBlock> iterateBlocks(Context context);

	default ContextValidator.Parameters parameters() {
		return settings().target().parameters();
	}

	default void run(Context context, Consumer<Context> onMatch, Predicate<Context> matchFilter, int limit) {
		settings().target().run(context, this, onMatch, matchFilter, limit);
	}

	static <A extends Area> Products.P1<RecordCodecBuilder.Mu<A>, Settings> addSettingsField(RecordCodecBuilder.Instance<A> instance) {
		return instance.group(Settings.CODEC.forGetter(Area::settings));
	}

	record Type<A extends Area>(MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.AREA_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.AREA_TYPE);

	}

	record Settings(Target target, Shape shape) {

		public static final MapCodec<Settings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Target.CODEC.fieldOf("target").forGetter(Settings::target),
			Shape.CODEC.fieldOf("shape").forGetter(Settings::shape)
		).apply(instance, Settings::new));

		public static final StreamCodec<ByteBuf, Settings> STREAM_CODEC = StreamCodec.composite(
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

				Iterable<Entity> entities = area.iterateEntities(context);
				int processed = 0;

				for (var entity : entities) {

					Context areaContext = new Context.Builder(context)
						.withRequired(ENTITY_IN_AREA, entity)
						.build(context.level());

					if (matchFilter.test(areaContext)) {
						onMatch.accept(areaContext);
						processed++;
					}

					if (limit > 0 && processed >= limit) {
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

				Iterable<CachedBlock> blocks = area.iterateBlocks(context);
				int processed = 0;

				for (var block : blocks) {

					Context areaContext = new Context.Builder(context)
						.withRequired(BLOCK_IN_AREA, block)
						.build(context.level());

					if (matchFilter.test(areaContext)) {
						onMatch.accept(areaContext);
						processed++;
					}

					if (limit > 0 && processed >= limit) {
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
