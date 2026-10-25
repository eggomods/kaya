package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record InflateBoxProvider(BoxProvider box, Vec3Provider vector) implements BoxProvider {

	public static final MapCodec<InflateBoxProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BoxProvider.CODEC.fieldOf("box").forGetter(InflateBoxProvider::box),
		Vec3Provider.CODEC.fieldOf("vector").forGetter(InflateBoxProvider::vector)
	).apply(instance, InflateBoxProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, InflateBoxProvider> STREAM_CODEC = StreamCodec.composite(
		BoxProvider.STREAM_CODEC, InflateBoxProvider::box,
		Vec3Provider.STREAM_CODEC, InflateBoxProvider::vector,
		InflateBoxProvider::new
	);

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.INFLATE;
	}

	@Override
	public Optional<AABB> getValue(Context context) {
		return box().getValue(context.forChild(".box"))
			.flatMap(box -> vector().getValue(context.forChild(".vector"))
				.map(translation -> this.inflate(box, translation)));
	}

	@Override
	public void validate(ContextValidator validator) {

		BoxProvider.super.validate(validator);

		box().validate(validator.forChild(".box"));
		vector().validate(validator.forChild(".vector"));

	}

	private AABB inflate(AABB box, Vec3 inflate) {
		return box.inflate(inflate.x(), inflate.y(), inflate.z());
	}

}
