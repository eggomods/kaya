package io.github.eggohito.kaya.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record EitherHolder<E>(Either<ResourceKey<E>, TagKey<E>> keyOrTag) implements ContextValidatable {

	@Override
	public void validate(ContextValidator validator) {
		keyOrTag()
			.ifLeft(key -> RegistryUtil.validateKey(validator.forChild(".key"), key))
			.ifRight(tag -> RegistryUtil.validateTag(validator.forChild(".tag"), tag));
	}

	public static <T> EitherHolder<T> key(ResourceKey<T> key) {
		return new EitherHolder<>(Either.left(key));
	}

	public static <T> EitherHolder<T> tag(TagKey<T> tag) {
		return new EitherHolder<>(Either.right(tag));
	}

	public static <T> MapCodec<EitherHolder<T>> mapCodec(ResourceKey<Registry<T>> registryKey) {
		return RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.mapEither(ResourceKey.codec(registryKey).fieldOf("key"), TagKey.hashedCodec(registryKey).fieldOf("tag")).forGetter(EitherHolder::keyOrTag))
			.apply(instance, EitherHolder::new)
		);
	}

	public static <T> Codec<EitherHolder<T>> codec(ResourceKey<Registry<T>> registryKey) {
		return mapCodec(registryKey).codec();
	}

	public static <T> StreamCodec<ByteBuf, EitherHolder<T>> streamCodec(ResourceKey<Registry<T>> registryKey) {
		return ByteBufCodecs.either(
			ResourceKey.streamCodec(registryKey),
			TagKey.streamCodec(registryKey)
		).map(
			EitherHolder::new,
			EitherHolder::keyOrTag
		);
	}

}
