package io.github.eggohito.kaya.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.StringDisplayable;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import io.github.eggohito.kaya.util.alias.ResourceLocationAlias;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ContextParameter<T> extends StringDisplayable {

	Codec<ContextParameter<?>> CODEC = Type.CODEC.dispatch(ContextParameter::getType, type -> type.codec().fieldOf("name"));

	StreamCodec<RegistryFriendlyByteBuf, ContextParameter<?>> STREAM_CODEC = Type.STREAM_CODEC.dispatch(ContextParameter::getType, Type::streamCodec);

	ResourceLocation name();

	Type<?> getType();

	@Override
	default String asDisplayString() {
		return this.name().toString() + " (type: \"" + this.getType() + "\")";
	}

	@Accessors(fluent = true)
	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	final class Type<P extends ContextParameter<?>> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.CONTEXT_PARAMETER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);
		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

		private final Codec<P> codec;
		private final StreamCodec<ByteBuf, P> streamCodec;
		private final ResourceLocationAlias aliases;
		private final Function<ResourceLocation, P> constructor;

		public P create(ResourceLocation id) {
			return constructor().apply(id);
		}

		@Override
		public String toString() {
			return Util.getRegisteredName(KayaRegistries.CONTEXT_PARAMETER_TYPE, this);
		}

		public static <P extends ContextParameter<?>> Type<P> of(Function<ResourceLocation, P> constructor, ResourceLocationAlias aliases) {
			Function<ResourceLocation, P> composed = constructor.compose(id -> aliases.resolve(id, Predicate.not(id::equals)));
			return new Type<>(
				KayaCodecs.MOD_NAMESPACE_IDENTIFIER.xmap(composed, ContextParameter::name),
				ResourceLocation.STREAM_CODEC.map(constructor, ContextParameter::name),
				aliases,
				composed
			);
		}

		public static <P extends ContextParameter<?>> Type<P> of(Function<ResourceLocation, P> constructor) {
			return of(constructor, new ResourceLocationAlias());
		}

	}

}
