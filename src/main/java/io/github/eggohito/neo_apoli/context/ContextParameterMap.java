package io.github.eggohito.neo_apoli.context;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public record ContextParameterMap(Map<ContextParameter<?>, ValueProvider<?>> map) implements ContextValidatable {

	public static final ContextParameterMap EMPTY = new ContextParameterMap(Map.of());

	private static final Codec<Map<ResourceLocation, ValueProvider<?>>> ID_AND_PROVIDER_CODEC = Codec.unboundedMap(NeoApoliCodecs.MOD_NAMESPACE_IDENTIFIER, ValueProvider.CODEC);
	private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, ValueProvider<?>>> ID_AND_PROVIDER_STREAM_CODEC = ByteBufCodecs.map(Object2ObjectLinkedOpenHashMap::new, ResourceLocation.STREAM_CODEC, ValueProvider.STREAM_CODEC);

	public static final Codec<ContextParameterMap> CODEC = ID_AND_PROVIDER_CODEC.xmap(ContextParameterMap::from, ContextParameterMap::convert);
	public static final StreamCodec<RegistryFriendlyByteBuf, ContextParameterMap> STREAM_CODEC = ID_AND_PROVIDER_STREAM_CODEC.map(ContextParameterMap::from, ContextParameterMap::convert);

	@Override
	public void validate(ContextValidator validator) {
		map().forEach((parameter, provider) -> provider.validate(validator.forChild(".\"" + parameter.name() + "\"")));
	}

	public ContextValidator.Parameters forValidation() {

		var builder = new ContextValidator.Parameters.Builder();
		map().keySet().forEach(builder::required);

		return builder.build();

	}

	@SuppressWarnings("unchecked")
	public Context forUser(Context baseContext, UnaryOperator<Context> parametersResolver) {

		var builder = new Context.Builder(baseContext);
		var resolved = parametersResolver.apply(baseContext);

		map().forEach((parameter, provider) -> builder.withOptional((ContextParameter<Object>) parameter, (Optional<Object>) provider.getValue(resolved.forChild(".\"" + parameter.name() + "\""))));
		return builder.build(baseContext.level());

	}

	private Map<ResourceLocation, ValueProvider<?>> convert() {

		Map<ResourceLocation, ValueProvider<?>> idAndProvider = new Object2ObjectLinkedOpenHashMap<>();
		map().forEach((parameter, provider) -> idAndProvider.put(parameter.name(), provider));

		return idAndProvider;

	}

	private static ContextParameterMap from(Map<ResourceLocation, ValueProvider<?>> idAndProvider) {

		Map<ContextParameter<?>, ValueProvider<?>> map = new Object2ObjectLinkedOpenHashMap<>();
		idAndProvider.forEach((id, provider) -> map.put(provider.getKind().createParameter(id), provider));

		return new ContextParameterMap(map);

	}

}
