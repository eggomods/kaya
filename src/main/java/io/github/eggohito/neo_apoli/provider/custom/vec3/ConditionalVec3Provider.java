package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record ConditionalVec3Provider(Condition condition, Vec3Provider onTrue, Vec3Provider onFalse) implements Vec3Provider, ConditionalValueProvider<Vec3, Vec3Provider> {

	public static final MapCodec<ConditionalVec3Provider> CODEC = ConditionalValueProvider.mapCodec(Vec3Provider.CODEC, ConditionalVec3Provider::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalVec3Provider> STREAM_CODEC = ConditionalValueProvider.streamCodec(Vec3Provider.STREAM_CODEC, ConditionalVec3Provider::new);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.CONDITIONAL;
	}

}
