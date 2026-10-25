package io.github.eggohito.neo_apoli.color.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorResolvers;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ColorResolver;

public record BiomeFoliageColor(Vec3Provider position, FloatProvider alpha, BooleanProvider dry) implements Color {

	public static final MapCodec<BiomeFoliageColor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("position").forGetter(BiomeFoliageColor::position),
		FloatProvider.clamped(0.0F, 1.0F).fieldOf("alpha").forGetter(BiomeFoliageColor::alpha),
		BooleanProvider.CODEC.fieldOf("dry").forGetter(BiomeFoliageColor::dry)
	).apply(instance, BiomeFoliageColor::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BiomeFoliageColor> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, BiomeFoliageColor::position,
		FloatProvider.STREAM_CODEC, BiomeFoliageColor::alpha,
		BooleanProvider.STREAM_CODEC, BiomeFoliageColor::dry,
		BiomeFoliageColor::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliColorTypes.BIOME_FOLIAGE;
	}

	@Override
	public int intValue(Context context) {

		BlockPos position = position().getValue(context.forChild(".position"))
			.map(BlockPos::containing)
			.orElse(null);

		if (position == null) {
			return 0;
		}

		float alphaFloat = alpha().getValue(context.forChild(".alpha")).orElse(1.0F);
		boolean dry = dry().getBoolean(context.forChild(".dry"));

		ColorResolver colorResolver = dry
			? NeoApoliColorResolvers.BIOME_DRY_FOLIAGE_COLOR
			: NeoApoliColorResolvers.BIOME_FOLIAGE_COLOR;

		return ARGB.color(ARGB.as8BitChannel(alphaFloat), context.level().getBlockTint(position, colorResolver));

	}

	@Override
	public void validate(ContextValidator validator) {
		Color.super.validate(validator);
		position().validate(validator.forChild(".position"));
		alpha().validate(validator.forChild(".alpha"));
		dry().validate(validator.forChild(".dry"));
	}

}
