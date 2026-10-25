package io.github.eggohito.neo_apoli.provider.custom.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.nbt.NbtProvider;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface NbtNumberProvider<N extends Number> extends ValueProvider<N> {

	@Override
	default void validate(ContextValidator validator) {
		ValueProvider.super.validate(validator);
		source().validate(validator.forChild(".source"));
	}

	NbtProvider source();

	NbtPathArgument.NbtPath path();

	default Optional<N> convertTag(Context context, Function<NumericTag, N> numeric, IntFunction<N> other) {

		Tag source = source()
			.getValue(context.forChild(".source"))
			.orElse(null);

		if (source == null) {
			return Optional.empty();
		}

		try {

			List<Tag> tags = path().get(source);
			int size = tags.size();

			if (size == 1) {

				N converted = switch (tags.getFirst()) {
					case NumericTag numericTag ->
						numeric.apply(numericTag);
					case CollectionTag collectionTag ->
						other.apply(collectionTag.size());
					case CompoundTag compoundTag ->
						other.apply(compoundTag.size());
					case StringTag(String value) ->
						other.apply(value.length());
					default ->
						null;
				};

				return Optional.ofNullable(converted);

			}

			else {
				return Optional.of(other.apply(path().countMatching(source)));
			}

		}

		catch (CommandSyntaxException e) {
			context.reportProblem("Error trying to get a numeric value in NBT path \"" + this.path() + " from NBT \"" + source + "\": " + e.getMessage());
		}

		return Optional.empty();

	}

	static <M extends NbtNumberProvider<?>> MapCodec<M> mapCodec(BiFunction<NbtProvider, NbtPathArgument.NbtPath, M> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			NbtProvider.CODEC.fieldOf("source").forGetter(NbtNumberProvider::source),
			NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(NbtNumberProvider::path)
		).apply(instance, constructor));
	}

	static <M extends NbtNumberProvider<?>> StreamCodec<RegistryFriendlyByteBuf, M> streamCodec(BiFunction<NbtProvider, NbtPathArgument.NbtPath, M> constructor) {
		return StreamCodec.composite(
			NbtProvider.STREAM_CODEC, NbtNumberProvider::source,
			NeoApoliStreamCodecs.NBT_PATH, NbtNumberProvider::path,
			constructor
		);
	}

}
