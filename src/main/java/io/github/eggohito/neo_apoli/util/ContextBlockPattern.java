package io.github.eggohito.neo_apoli.util;

import com.google.common.base.Joiner;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.condition.custom.ConstantCondition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.mixin.access.BlockPatternAccessor;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Accessors(fluent = true)
public final class ContextBlockPattern implements ContextValidatable {

	public static final BlockContextParameter MATCHING_BLOCK = new BlockContextParameter(NeoApoli.id("matching_block"));
	public static final ContextValidator.Parameters MATCHING_PARAMETER_SET = new ContextValidator.Parameters.Builder().required(MATCHING_BLOCK).build();
	
	private static final Joiner COMMA_JOINED = Joiner.on(", ");
	private static final char RESERVED_SYMBOL = ' ';

	private static final Codec<Char2ObjectMap<Condition>> LOOKUP_CODEC = Codec.unboundedMap(NeoApoliCodecs.CHARACTER.validate(ContextBlockPattern::validateSymbol), Condition.CODEC).xmap(Char2ObjectArrayMap::new, Function.identity());
	private static final StreamCodec<RegistryFriendlyByteBuf, Char2ObjectMap<Condition>> LOOKUP_STREAM_CODEC = ByteBufCodecs.map(Char2ObjectArrayMap::new, NeoApoliStreamCodecs.CHARACTER, Condition.STREAM_CODEC);

	private static final StreamCodec<ByteBuf, char[][][]> CHAR_ARRAY_STREAM_CODEC = new StreamCodec<>() {

		@Override
		public char @NotNull [][][] decode(ByteBuf buf) {

			int depth = buf.readInt();
			int height = buf.readInt();
			int width = buf.readInt();

			char[][][] array = new char[depth][height][width];

			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						array[z][y][x] = buf.readChar();
					}
				}
			}

			return array;

		}

		@Override
		public void encode(ByteBuf buf, char[][][] array) {

			int depth = array.length;
			int height = depth > 0 ? array[0].length : 0;
			int width = height > 0 ? array[0][0].length : 0;

			buf.writeInt(depth);
			buf.writeInt(height);
			buf.writeInt(width);

			for (char[][] chars : array) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						buf.writeChar(chars[y][x]);
					}
				}
			}

		}

	};

	public static final MapCodec<ContextBlockPattern> MAP_CODEC = Data.MAP_CODEC.flatXmap(
		ContextBlockPattern::unpack,
		ContextBlockPattern::pack
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextBlockPattern> STREAM_CODEC = StreamCodec.composite(
		CHAR_ARRAY_STREAM_CODEC, pattern -> pattern.pattern,
		LOOKUP_STREAM_CODEC, pattern -> pattern.where,
		ContextBlockPattern::new
	);

	private final char[][][] pattern;
	private final Char2ObjectMap<Condition> where;

	@Getter
	private final int depth;
	@Getter
	private final int height;
	@Getter
	private final int width;

	ContextBlockPattern(char[][][] pattern, Char2ObjectMap<Condition> where) {
		this.pattern = pattern;
		this.where = where;
		this.depth = pattern.length;
		this.height = this.depth > 0 ? pattern[0].length : 0;
		this.width = this.height > 0 ? pattern[0][0].length : 0;
	}

	@Override
	public void validate(ContextValidator validator) {

		ContextValidator keysValidator = validator
			.forChild(".where")
			.withParams(MATCHING_PARAMETER_SET);

		this.where.forEach((symbol, condition) -> keysValidator.forChild("." + symbol).validate(condition));

	}

	public DataResult<Data> pack() {

		List<List<String>> pattern = new ObjectArrayList<>();

		for (int z = 0; z < depth(); z++) {

			List<String> aisle = new ObjectArrayList<>();

			for (int y = 0; y < height(); y++) {

				StringBuilder row = new StringBuilder();

				for (int x = 0; x < width(); x++) {
					row.append(this.pattern[z][y][x]);
				}

				aisle.add(row.toString());

			}

			pattern.add(aisle);

		}

		Char2ObjectMap<Condition> whereCopy = new Char2ObjectArrayMap<>(this.where);
		whereCopy.remove(RESERVED_SYMBOL);

		return DataResult.success(new Data(pattern, whereCopy));

	}

	public Result check(Context context, BlockPos frontTopLeft, Direction forwards, Direction up) {
		LoadingCache<BlockPos, BlockInWorld> cache = BlockPattern.createLevelCache(context.level(), false);
		return this.checkInternally(cache, context, frontTopLeft, forwards, up);
	}

	private Result checkInternally(LoadingCache<BlockPos, BlockInWorld> getter, Context context, BlockPos frontTopLeft, Direction forwards, Direction up) {

		LongSet mismatches = new LongArraySet();
		LongSet matches = new LongArraySet();

		tryIterate:
		for (int z = 0; z < depth(); z++) {
			for (int y = 0; y < height(); y++) {
				for (int x = 0; x < width(); x++) {
					
					BlockPos pos;
					long posLong;
					
					try {
						pos = BlockPatternAccessor.callTranslateAndRotate(frontTopLeft, forwards, up, x, y, z);
						posLong = pos.asLong();
					}
					
					catch (Exception e) {
						break tryIterate;
					}

					Context matchingContext = new Context.Builder(context)
						.withOptional(MATCHING_BLOCK, CachedBlock.optionallyFromWorld(getter.getUnchecked(pos)))
						.build(context.level());

					char symbol = this.pattern[z][y][x];
					Condition condition = this.where.get(symbol);

					if (condition == null || condition.test(matchingContext.forChild("." + symbol))) {
						matches.add(posLong);
					}

					else {
						mismatches.add(posLong);
					}

				}
			}
		}

		return new Result(this, frontTopLeft, forwards, up, matches, mismatches);

	}

	public static DataResult<ContextBlockPattern> unpack(Data data) {

		try {

			Builder builder = new Builder();

			data.pattern().forEach(builder::aisle);
			data.where().forEach(builder::where);

			return DataResult.success(builder.build());

		}

		catch (Exception e) {
			return DataResult.error(e::getMessage);
		}

	}

	private static DataResult<Character> validateSymbol(char ch) {
		return ch == RESERVED_SYMBOL
			? DataResult.error(() -> "The character ' ' (whitespace) is a reserved symbol!")
			: DataResult.success(ch);
	}

	private static void ensureSymbolIsValid(char ch) {
		validateSymbol(ch).getOrThrow();
	}

	public static final class Builder {

		private final List<List<String>> pattern = new ObjectArrayList<>();
		private final Char2ObjectMap<Condition> where = new Char2ObjectArrayMap<>();

		private int height;
		private int width;
		
		public Builder() {
			this.where.put(RESERVED_SYMBOL, new ConstantCondition(true));
		}

		public Builder aisle(List<String> aisle) {

			if (aisle.isEmpty() || aisle.getFirst().isEmpty()) {
				throw new IllegalArgumentException("Empty aisles are not allowed!");
			}

			if (this.pattern.isEmpty()) {
				this.height = aisle.size();
				this.width = aisle.getFirst().length();
			}

			if (aisle.size() != this.height) {
				throw new IllegalArgumentException("Expected aisles to have a height of " + this.height + ", but found one with a height of " + aisle.size() + "!");
			}

			for (var row : aisle) {

				if (row.length() != this.width) {
					throw new IllegalArgumentException("Expected rows of aisles to have a width of " + this.width + ", but found one with a width of " + row.length() + "!");
				}

				for (var symbol : row.toCharArray()) {
					this.where.putIfAbsent(symbol, null);
				}

			}

			this.pattern.add(aisle);
			return this;

		}

		public Builder aisle(String... aisle) {
			return this.aisle(Arrays.asList(aisle));
		}

		public Builder where(char symbol, @NotNull Condition condition) {

			ensureSymbolIsValid(symbol);
			this.where.put(symbol, condition);

			return this;

		}

		public ContextBlockPattern build() {

			CharSet missingConditions = new CharArraySet();
			char[][][] pattern = new char[this.pattern.size()][this.height][this.width];

			for (var entry : this.where.char2ObjectEntrySet()) {

				char symbol = entry.getCharKey();
				Condition condition = entry.getValue();

				if (condition == null) {
					missingConditions.add(symbol);
				}

			}

			if (!missingConditions.isEmpty()) {
				throw new IllegalStateException("Conditions of the following symbol(s) are missing: [" + COMMA_JOINED.join(missingConditions) + "]");
			}

			for (int z = 0; z < this.pattern.size(); z++) {
				for (int y = 0; y < this.height; y++) {
					for (int x = 0; x < this.width; x++) {
						pattern[z][y][x] = this.pattern.get(z).get(y).charAt(x);
					}
				}
			}

			return new ContextBlockPattern(pattern, this.where);

		}

	}

	public record Data(List<List<String>> pattern, Char2ObjectMap<Condition> where) {

		public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.listOf().listOf().fieldOf("pattern").forGetter(Data::pattern),
			LOOKUP_CODEC.fieldOf("where").forGetter(Data::where)
		).apply(instance, Data::new));

	}

	public record Result(BlockPos frontTopLeft, Direction forwards, Direction up, LongSet matches, LongSet mismatches, int depth, int height, int width) {

		Result(ContextBlockPattern pattern, BlockPos frontTopLeft, Direction forwards, Direction up, LongSet matches, LongSet mismatches) {
			this(frontTopLeft, forwards, up, matches, mismatches, pattern.depth(), pattern.height(), pattern.width());
		}

	}

}
