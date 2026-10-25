package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalBoxProvider(List<CompositeConditional.Entry<BoxProvider>> entries, BoxProvider defaultValue) implements BoxProvider, CompositeConditionalValueProvider<AABB, BoxProvider> {

	public static final MapCodec<CompositeConditionalBoxProvider> CODEC = CompositeConditionalValueProvider.mapCodec(BoxProvider.CODEC, CompositeConditionalBoxProvider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalBoxProvider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(BoxProvider.STREAM_CODEC, CompositeConditionalBoxProvider::new);

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
