package io.github.eggohito.kaya.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.registry.provider.KayaVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record NormalizeVec3Provider(Vec3Provider vector) implements Vec3Provider {

	public static final MapCodec<NormalizeVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Vec3Provider.CODEC.fieldOf("vector").forGetter(NormalizeVec3Provider::vector))
		.apply(instance, NormalizeVec3Provider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, NormalizeVec3Provider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, NormalizeVec3Provider::vector,
		NormalizeVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return KayaVec3ProviderTypes.NORMALIZE;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return vector().getValue(context.forChild(".vector")).map(Vec3::normalize);
	}

	@Override
	public void validate(ContextValidator validator) {
		Vec3Provider.super.validate(validator);
		vector().validate(validator.forChild(".vector"));
	}

}
