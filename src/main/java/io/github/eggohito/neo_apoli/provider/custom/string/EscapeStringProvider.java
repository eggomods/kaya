package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EscapeStringProvider(StringProvider string) implements StringProvider {

	public static final MapCodec<EscapeStringProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(StringProvider.CODEC.fieldOf("string").forGetter(EscapeStringProvider::string))
		.apply(instance, EscapeStringProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, EscapeStringProvider> STREAM_CODEC = StreamCodec.composite(
		StringProvider.STREAM_CODEC, EscapeStringProvider::string,
		EscapeStringProvider::new
	);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.ESCAPE;
	}

	@Override
	public Optional<String> getValue(Context context) {
		return string()
			.getValue(context.forChild(".string"))
			.map(EscapeStringProvider::escape);
	}

	@Override
	public void validate(ContextValidator validator) {
		StringProvider.super.validate(validator);
		string().validate(validator.forChild(".string"));
	}

	private static String escape(String string) {

		StringBuilder builder = new StringBuilder();
		int length = string.length();

		for (int i = 0; i < length; i++) {

			char ch = string.charAt(i);
			String escapedCh = SnbtGrammar.escapeControlCharacters(ch);

			switch (ch) {
				case '\\' ->
					builder.append("\\\\");
				case '"', '\'' ->
					builder.append("\\").append(ch);
				default -> {

					if (escapedCh != null) {
						builder.append('\\').append(escapedCh);
					}

					else {
						builder.append(ch);
					}

				}
			}

		}

		return builder.toString();

	}

}
