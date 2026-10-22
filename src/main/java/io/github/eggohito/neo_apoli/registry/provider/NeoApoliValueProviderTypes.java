package io.github.eggohito.neo_apoli.registry.provider;

import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliValueProviderTypes {

	public static void registerAll() {
		NeoApoliBlockProviderTypes.registerAll();
		NeoApoliBooleanProviderTypes.registerAll();
		NeoApoliBoxProviderTypes.registerAll();
		NeoApoliCommandSourceProviderTypes.registerAll();
		NeoApoliDamageSourceProviderTypes.registerAll();
		NeoApoliDirectionProviderTypes.registerAll();
		NeoApoliEffectProviderTypes.registerAll();
		NeoApoliEntityProviderTypes.registerAll();
		NeoApoliFloatProviderTypes.registerAll();
		NeoApoliIntProviderTypes.registerAll();
		NeoApoliItemProviderTypes.registerAll();
		NeoApoliNbtProviderTypes.registerAll();
		NeoApoliSlotProviderTypes.registerAll();
		NeoApoliStringProviderTypes.registerAll();
		NeoApoliVec3ProviderTypes.registerAll();
	}

	public static <P extends ValueProvider<?>> ValueProvider.Kind<P> register(ResourceLocation id, ValueProvider.Kind<P> kind) {
		return Registry.register(NeoApoliRegistries.VALUE_PROVIDER_KIND, id, kind);
	}

	private static <P extends ValueProvider<?>> ValueProvider.Kind<P> registerInternal(String path, ValueProvider.Kind<P> kind) {
		return register(NeoApoli.id(path), kind);
	}

}
