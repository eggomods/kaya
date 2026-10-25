package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record JoinStringProvider(List<StringProvider> strings, StringProvider separator) implements StringProvider {

	public static final MapCodec<JoinStringProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(StringProvider.CODEC.listOf()).fieldOf("strings").forGetter(JoinStringProvider::strings),
		StringProvider.CODEC.fieldOf("separator").forGetter(JoinStringProvider::separator)
	).apply(instance, JoinStringProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, JoinStringProvider> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(ObjectArrayList::new, StringProvider.STREAM_CODEC), JoinStringProvider::strings,
		StringProvider.STREAM_CODEC, JoinStringProvider::separator,
		JoinStringProvider::new
	);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.JOIN;
	}

	@Override
	public Optional<String> getValue(Context context) {

		StringBuilder result = new StringBuilder();
		MutableBoolean init = new MutableBoolean(false);

		MiscUtil.iterateList(
			strings(),
			(index, provider) -> {

				String string = provider
					.getValue(context.forChild(".strings[" + index + "]"))
					.orElse(null);

				if (string != null) {

					if (init.isTrue()) {
						separator()
							.getValue(context.forChild(".separator"))
							.ifPresent(separator -> result.append(separator).append(string));

					}

					else {
						result.append(string);
						init.setTrue();
					}

				}

			}
		);

		return init.isTrue()
			? Optional.of(result.toString())
			: Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		StringProvider.super.validate(validator);
		ContextValidatable.validate(strings(), validator, index -> ".strings[" + index + "]");
		separator().validate(validator.forChild(".separator"));
	}

}
