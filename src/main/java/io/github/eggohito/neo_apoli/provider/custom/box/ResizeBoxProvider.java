package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ResizeBoxProvider(BoxProvider box, BoxProvider offset) implements BoxProvider {

	public static final MapCodec<ResizeBoxProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BoxProvider.CODEC.fieldOf("box").forGetter(ResizeBoxProvider::box),
		BoxProvider.CODEC.fieldOf("offset").forGetter(ResizeBoxProvider::offset)
	).apply(instance, ResizeBoxProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ResizeBoxProvider> STREAM_CODEC = StreamCodec.composite(
		BoxProvider.STREAM_CODEC, ResizeBoxProvider::box,
		BoxProvider.STREAM_CODEC, ResizeBoxProvider::offset,
		ResizeBoxProvider::new
	);

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.RESIZE;
	}

	@Override
	public Optional<AABB> getValue(Context context) {
		return box().getValue(context.forChild(".box"))
			.flatMap(box -> offset().getValue(context.forChild(".offset"))
				.map(offset -> this.resize(box, offset)));
	}

	@Override
	public void validate(ContextValidator validator) {

		BoxProvider.super.validate(validator);

		box().validate(validator.forChild(".box"));
		offset().validate(validator.forChild(".offset"));

	}

	private AABB resize(AABB box, AABB offset) {
		return new AABB(
			box.minX + offset.minX,
			box.minY + offset.minY,
			box.minZ + offset.minZ,
			box.maxX + offset.maxX,
			box.maxY + offset.maxY,
			box.maxZ + offset.maxZ
		);
	}

}
