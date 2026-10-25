package io.github.eggohito.neo_apoli.area.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.area.Area;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.box.BoxProvider;
import io.github.eggohito.neo_apoli.registry.area.NeoApoliAreaTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.function.Function;

public record BoxArea(Area.Settings settings, BoxProvider box) implements Area {

	public static final MapCodec<BoxArea> CODEC = RecordCodecBuilder.mapCodec(instance ->
		Area.addSettingsField(instance)
			.and(BoxProvider.CODEC.fieldOf("box").forGetter(BoxArea::box))
			.apply(instance, BoxArea::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, BoxArea> STREAM_CODEC = StreamCodec.composite(
		Settings.STREAM_CODEC, BoxArea::settings,
		BoxProvider.STREAM_CODEC, BoxArea::box,
		BoxArea::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliAreaTypes.BOX;
	}

	@Override
	public Iterable<Entity> iterateEntities(Context context) {
		return unwrap(context, box -> settings().shape().iterateEntitiesWithinBox(context.level(), box));
	}

	@Override
	public Iterable<CachedBlock> iterateBlocks(Context context) {
		return unwrap(context, box -> settings().shape().iterateBlocksWithinBox(context.level(), box));
	}

	@Override
	public void validate(ContextValidator validator) {
		box().validate(validator.forChild(".box"));
	}

	private <R> Iterable<R> unwrap(Context context, Function<AABB, Iterable<R>> applier) {
		return box().getValue(context.forChild(".box"))
			.map(applier)
			.orElse(MiscUtil.emptyIterable());
	}

}
