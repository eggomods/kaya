package io.github.eggohito.kaya.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.context.parameter.ContextParameter;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ValueProvider<V> extends ContextUser {

	Codec<ValueProvider<?>> CODEC = Kind.CODEC.dispatch(ValueProvider::getKind, kind -> kind.codec().fieldOf("value"));

	StreamCodec<RegistryFriendlyByteBuf, ValueProvider<?>> STREAM_CODEC = Kind.STREAM_CODEC.dispatch(ValueProvider::getKind, Kind::streamCodec);

	@NotNull
	Type<?> getType();

	@NotNull
	Kind<?> getKind();

	Optional<V> getValue(Context context);

	interface Type<P extends ValueProvider<?>> {

		MapCodec<P> mapCodec();

		StreamCodec<RegistryFriendlyByteBuf, P> streamCodec();

	}

	@Accessors(fluent = true)
	@Getter
	final class Kind<P extends ValueProvider<?>> {

		public static final FixedRegistryAlias<Kind<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.VALUE_PROVIDER_KIND, Kaya.GLOBAL_ALIASES);

		public static final Codec<Kind<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);
		public static final StreamCodec<RegistryFriendlyByteBuf, Kind<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

		private final Codec<P> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, P> streamCodec;
		private final ContextParameter.Type<?> parameterType;

		private Kind(Codec<P> codec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec, ContextParameter.Type<?> parameterType) {
			this.codec = codec;
			this.streamCodec = streamCodec;
			this.parameterType = parameterType;
		}

		public ContextParameter<?> createParameter(ResourceLocation id) {
			return parameterType().create(id);
		}

		public static <V, Parameter extends ContextParameter<V>, Provider extends ValueProvider<V>> Kind<Provider> of(Codec<Provider> codec, StreamCodec<RegistryFriendlyByteBuf, Provider> streamCodec, ContextParameter.Type<Parameter> parameterType) {
			return new Kind<>(codec, streamCodec, parameterType);
		}

	}

}
