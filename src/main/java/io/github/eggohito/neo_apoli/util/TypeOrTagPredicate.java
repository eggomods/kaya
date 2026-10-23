package io.github.eggohito.neo_apoli.util;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;

public record TypeOrTagPredicate<T>(Optional<ResourceKey<T>> key, Optional<TagKey<T>> tag) implements ContextValidatable {

	@Override
	public void validate(ContextValidator validator) {
		validate(this, Optional.empty())
			.ifError(error -> validator.reportProblem(error.message()))
			.ifSuccess(self -> {
				self.key().ifPresent(key -> RegistryUtil.validateKey(validator.forChild(".key"), key));
				self.tag().ifPresent(tag -> RegistryUtil.validateTag(validator.forChild(".tag"), tag));
			});
	}

	public boolean test(Predicate<ResourceKey<T>> keyPredicate, Predicate<TagKey<T>> tagPredicate) {
		return (key().isEmpty() || keyPredicate.test(key().orElseThrow()))
			&& (tag().isEmpty() || tagPredicate.test(tag().orElseThrow()));
	}

	public static <T> MapCodec<TypeOrTagPredicate<T>> mapCodec(Codec<ResourceKey<T>> keyCodec, Codec<TagKey<T>> tagCodec) {

		MapCodec<TypeOrTagPredicate<T>> unvalidated = RecordCodecBuilder.mapCodec(instance -> instance.group(
			keyCodec.optionalFieldOf("key").forGetter(TypeOrTagPredicate::key),
			tagCodec.optionalFieldOf("tag").forGetter(TypeOrTagPredicate::tag)
		).apply(instance, TypeOrTagPredicate::new));

		return unvalidated.mapResult(new MapCodec.ResultFunction<>() {

			@Override
			public <I> DataResult<TypeOrTagPredicate<T>> apply(DynamicOps<I> ops, MapLike<I> input, DataResult<TypeOrTagPredicate<T>> result) {
				return result.flatMap(predicate -> validate(predicate, Optional.of(input)));
			}

			@Override
			public <O> RecordBuilder<O> coApply(DynamicOps<O> ops, TypeOrTagPredicate<T> input, RecordBuilder<O> prefix) {
				return prefix;
			}

		});

	}

	public static <B extends ByteBuf, T> StreamCodec<B, TypeOrTagPredicate<T>> streamCodec(StreamCodec<B, ResourceKey<T>> keyCodec, StreamCodec<B, TagKey<T>> tagCodec) {
		return StreamCodec.composite(
			ByteBufCodecs.optional(keyCodec), TypeOrTagPredicate::key,
			ByteBufCodecs.optional(tagCodec), TypeOrTagPredicate::tag,
			TypeOrTagPredicate::new
		);
	}

	private static <I, T> DataResult<TypeOrTagPredicate<T>> validate(TypeOrTagPredicate<T> predicate, Optional<MapLike<I>> input) {

		if (predicate.key().isEmpty() && predicate.tag().isEmpty()) {
			return DataResult.error(() -> "Any of 'key' or 'value' fields must be present" + input.map(self -> " in input: " + self).orElse("!"));
		}

		else if (predicate.key().isPresent() && predicate.tag().isPresent()) {
			return DataResult.error(() -> "Both 'key' and 'value' cannot be present at the same time" + input.map(self -> " in input: " + self).orElse("!"));
		}

		else {
			return DataResult.success(predicate);
		}

	}

}
