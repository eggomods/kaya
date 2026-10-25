package io.github.eggohito.neo_apoli.container_menu.custom;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.container_menu.ContainerMenu;
import io.github.eggohito.neo_apoli.registry.NeoApoliContainerMenuTypes;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import io.github.eggohito.neo_apoli.util.TextAlignment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuConstructor;

//  TODO: Finish the implementation of dynamic container menu
public record DynamicContainerMenu(TextAlignment titleAlignment, ResourceLocation texture, int columns, int rows) implements ContainerMenu {

	private static final MapCodec<DynamicContainerMenu> UNVALIDATED_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TextAlignment.CODEC.optionalFieldOf("title_alignment", TextAlignment.CENTER).forGetter(DynamicContainerMenu::titleAlignment),
		ResourceLocation.CODEC.fieldOf("texture").forGetter(DynamicContainerMenu::texture),
		ExtraCodecs.POSITIVE_INT.fieldOf("columns").forGetter(DynamicContainerMenu::columns),
		ExtraCodecs.POSITIVE_INT.fieldOf("rows").forGetter(DynamicContainerMenu::rows)
	).apply(instance, DynamicContainerMenu::new));

	public static final MapCodec<DynamicContainerMenu> CODEC = UNVALIDATED_CODEC.mapResult(new MapCodec.ResultFunction<>() {

		@Override
		public <T> DataResult<DynamicContainerMenu> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<DynamicContainerMenu> result) {
			return result.flatMap(menu -> DataResult.error(() -> "Container menu type \"" + RegistryUtil.getId(NeoApoliRegistries.CONTAINER_MENU_TYPE, menu.getType()) + "\" is not fully implemented yet!"));
		}

		@Override
		public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, DynamicContainerMenu input, RecordBuilder<T> prefix) {
			return prefix;
		}

	});

	public static final StreamCodec<RegistryFriendlyByteBuf, DynamicContainerMenu> STREAM_CODEC = StreamCodec.composite(
		TextAlignment.STREAM_CODEC, DynamicContainerMenu::titleAlignment,
		ResourceLocation.STREAM_CODEC, DynamicContainerMenu::texture,
		ByteBufCodecs.INT, DynamicContainerMenu::columns,
		ByteBufCodecs.INT, DynamicContainerMenu::rows,
		DynamicContainerMenu::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliContainerMenuTypes.DYNAMIC;
	}

	@Override
	public MenuConstructor constructor(Container container) {
		throw new IllegalStateException("Dynamic container menu is not fully implemented yet!");
	}

}
