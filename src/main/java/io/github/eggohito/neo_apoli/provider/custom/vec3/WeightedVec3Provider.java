package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.provider.WeightedValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record WeightedVec3Provider(WeightedList<Vec3Provider> entries) implements Vec3Provider, WeightedValueProvider<Vec3, Vec3Provider> {

	public static final MapCodec<WeightedVec3Provider> CODEC = WeightedValueProvider.mapCodec(Vec3Provider.CODEC, WeightedVec3Provider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, WeightedVec3Provider> STREAM_CODEC = WeightedValueProvider.streamCodec(Vec3Provider.STREAM_CODEC, WeightedVec3Provider::new);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.WEIGHTED;
	}

}
