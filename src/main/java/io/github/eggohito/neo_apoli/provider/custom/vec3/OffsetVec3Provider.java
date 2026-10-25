package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record OffsetVec3Provider(Vec3Provider vector, Vec3Provider offset) implements Vec3Provider {

	public static final MapCodec<OffsetVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("vector").forGetter(OffsetVec3Provider::vector),
		Vec3Provider.CODEC.fieldOf("offset").forGetter(OffsetVec3Provider::offset)
	).apply(instance, OffsetVec3Provider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, OffsetVec3Provider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, OffsetVec3Provider::vector,
		Vec3Provider.STREAM_CODEC, OffsetVec3Provider::offset,
		OffsetVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.OFFSET;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return vector().getValue(context.forChild(".vector"))
			.flatMap(vector -> offset().getValue(context.forChild(".offset"))
				.map(vector::add));
	}

	@Override
	public void validate(ContextValidator validator) {

		Vec3Provider.super.validate(validator);

		vector().validate(validator.forChild(".vector"));
		offset().validate(validator.forChild(".offset"));

	}

}
