package io.github.eggohito.kaya.util;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SuggestionProviderHelper {

	public static <T> CompletableFuture<Suggestions> suggestResources(Iterable<T> resources, SuggestionsBuilder builder, Function<T, ResourceLocation> idConverter, Function<String, Component> componentConverter) {

		String string = builder.getRemaining().toLowerCase(Locale.ROOT);
		SharedSuggestionProvider.filterResources(resources, string, idConverter, t -> {

			String id = idConverter.apply(t).toString();
			Component component = componentConverter.apply(id);

			builder.suggest(id, component);

		});

		return builder.buildFuture();

	}

	public static <T> CompletableFuture<Suggestions> suggestResources(Iterable<T> resources, SuggestionsBuilder builder, Function<T, ResourceLocation> idConverter) {
		return suggestResources(resources, builder, idConverter, Component::literal);
	}

}
