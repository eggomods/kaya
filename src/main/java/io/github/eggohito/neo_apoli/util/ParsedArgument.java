package io.github.eggohito.neo_apoli.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.flag.FeatureFlags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record ParsedArgument<A>(String input, A argument) {

	public static <A, T extends ArgumentType<A>> Codec<ParsedArgument<A>> codecWithContext(Function<HolderLookup.Provider, T> getter) {
		return new Codec<>() {

			@Override
			public <I> DataResult<Pair<ParsedArgument<A>, I>> decode(DynamicOps<I> ops, I input) {

				if (!(ops instanceof RegistryOps<I> registryOps)) {
					return DataResult.error(() -> "The passed dynamic ops is not a registry ops!");
				}

				HolderLookup.Provider registry = CodecUtil
					.getRegistries(registryOps)
					.orElse(null);

				if (registry == null) {
					return DataResult.error(() -> "The passed registry ops doesn't have a lookup provider!");
				}

				return Codec.STRING.parse(registryOps, input)
					.flatMap(string -> ParsedArgument.parse(getter.apply(registry), string)
						.map(parsed -> Pair.of(parsed, input)));

			}

			@Override
			public <O> DataResult<O> encode(ParsedArgument<A> parsed, DynamicOps<O> ops, O prefix) {
				return Codec.STRING.encode(parsed.input(), ops, prefix);
			}

		};
	}

	public static <A, T extends ArgumentType<A>> Codec<ParsedArgument<A>> codecWithSimpleContext(Function<CommandBuildContext, T> getter) {
		return codecWithContext(provider -> getter.apply(CommandBuildContext.simple(provider, FeatureFlags.DEFAULT_FLAGS)));
	}

	public static <A, T extends ArgumentType<A>> StreamCodec<RegistryFriendlyByteBuf, ParsedArgument<A>> streamCodecWithContext(Function<HolderLookup.Provider, T> getter) {
		return new StreamCodec<>() {

			@Override
			public @NotNull ParsedArgument<A> decode(RegistryFriendlyByteBuf buf) {

				RegistryAccess registryAccess = buf.registryAccess();
				String input = ByteBufCodecs.STRING_UTF8.decode(buf);

				return parse(getter.apply(registryAccess), input).getOrThrow();

			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, ParsedArgument<A> parsed) {
				ByteBufCodecs.STRING_UTF8.encode(buf, parsed.input());
			}

		};
	}

	public static <A, T extends ArgumentType<A>> StreamCodec<RegistryFriendlyByteBuf, ParsedArgument<A>> streamCodecWithSimpleContext(Function<CommandBuildContext, T> getter) {
		return streamCodecWithContext(provider -> getter.apply(CommandBuildContext.simple(provider, FeatureFlags.DEFAULT_FLAGS)));
	}

	public static <A, T extends ArgumentType<A>> Codec<ParsedArgument<A>> codec(T argumentType) {
		return Codec.STRING.comapFlatMap(input -> parse(argumentType, input), ParsedArgument::input);
	}

	public static <B extends ByteBuf, A, T extends ArgumentType<A>> StreamCodec<B, ParsedArgument<A>> streamCodec(T argumentType) {
		return ByteBufCodecs.STRING_UTF8.map(input -> parse(argumentType, input).getOrThrow(), ParsedArgument::input).cast();
	}

	private static <A, T extends ArgumentType<A>> DataResult<ParsedArgument<A>> parse(T type, String input) {

		try {

			StringReader reader = new StringReader(input);
			A argument = type.parse(reader);

			return DataResult.success(new ParsedArgument<>(input, argument));

		}

		catch (CommandSyntaxException e) {
			return DataResult.error(e::getMessage);
		}

	}

}
