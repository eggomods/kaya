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

public record ProductVec3Provider(Vec3Provider first, Vec3Provider second) implements Vec3Provider {

	public static final MapCodec<ProductVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("first").forGetter(ProductVec3Provider::first),
		Vec3Provider.CODEC.fieldOf("second").forGetter(ProductVec3Provider::second)
	).apply(instance, ProductVec3Provider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ProductVec3Provider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, ProductVec3Provider::first,
		Vec3Provider.STREAM_CODEC, ProductVec3Provider::second,
		ProductVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return KayaVec3ProviderTypes.PRODUCT;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return first().getValue(context.forChild(".first"))
			.flatMap(first -> second().getValue(context.forChild(".second"))
				.map(first::multiply));
	}

	@Override
	public void validate(ContextValidator validator) {
		Vec3Provider.super.validate(validator);
		first().validate(validator.forChild(".first"));
		second().validate(validator.forChild(".second"));
	}

}
