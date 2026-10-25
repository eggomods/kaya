package io.github.eggohito.neo_apoli.registry.area;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.area.Area;
import io.github.eggohito.neo_apoli.area.custom.BoxArea;
import io.github.eggohito.neo_apoli.area.custom.RadiusArea;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class NeoApoliAreaTypes {

	public static final Area.Type<RadiusArea> RADIUS = registerInternal("radius", RadiusArea.CODEC, RadiusArea.STREAM_CODEC);
	public static final Area.Type<BoxArea> BOX = registerInternal("box", BoxArea.CODEC, BoxArea.STREAM_CODEC);

	public static void registerAll() {

	}

	public static <A extends Area> Area.Type<A> register(ResourceLocation id, MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {
		return Registry.register(NeoApoliRegistries.AREA_TYPE, id, new Area.Type<>(mapCodec, streamCodec));
	}

	private static <A extends Area> Area.Type<A> registerInternal(String path, MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

}
