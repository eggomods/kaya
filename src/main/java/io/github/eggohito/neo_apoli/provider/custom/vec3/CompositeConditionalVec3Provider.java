package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.CompositeConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompositeConditionalVec3Provider(List<CompositeConditional.Entry<Vec3Provider>> entries, Vec3Provider defaultValue) implements Vec3Provider, CompositeConditionalValueProvider<Vec3, Vec3Provider> {

	public static final MapCodec<CompositeConditionalVec3Provider> CODEC = CompositeConditionalValueProvider.mapCodec(Vec3Provider.CODEC, CompositeConditionalVec3Provider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, CompositeConditionalVec3Provider> STREAM_CODEC = CompositeConditionalValueProvider.streamCodec(Vec3Provider.STREAM_CODEC, CompositeConditionalVec3Provider::new);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.COMPOSITE_CONDITIONAL;
	}

}
