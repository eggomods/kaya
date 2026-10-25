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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record TranslateBoxProvider(BoxProvider box, Vec3Provider vector) implements BoxProvider {

	public static final MapCodec<TranslateBoxProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BoxProvider.CODEC.fieldOf("box").forGetter(TranslateBoxProvider::box),
		Vec3Provider.CODEC.fieldOf("vector").forGetter(TranslateBoxProvider::vector)
	).apply(instance, TranslateBoxProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TranslateBoxProvider> STREAM_CODEC = StreamCodec.composite(
		BoxProvider.STREAM_CODEC, TranslateBoxProvider::box,
		Vec3Provider.STREAM_CODEC, TranslateBoxProvider::vector,
		TranslateBoxProvider::new
	);

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.TRANSLATE;
	}

	@Override
	public Optional<AABB> getValue(Context context) {
		return box().getValue(context.forChild(".box"))
			.flatMap(box -> vector().getValue(context.forChild(".vector"))
				.map(box::move));
	}

	@Override
	public void validate(ContextValidator validator) {
		BoxProvider.super.validate(validator);
		box().validate(validator.forChild(".box"));
		vector().validate(validator.forChild(".vector"));
	}

}
