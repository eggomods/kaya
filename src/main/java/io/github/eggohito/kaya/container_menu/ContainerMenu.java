package io.github.eggohito.kaya.container_menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.codec.MultiAlternativeCodec;
import io.github.eggohito.kaya.container_menu.custom.SimpleContainerMenu;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuConstructor;

public interface ContainerMenu {

	Codec<ContainerMenu> CODEC = Codec.lazyInitialized(() -> new MultiAlternativeCodec<>(Type.CODEC.dispatch(ContainerMenu::getType, Type::mapCodec), SimpleContainerMenu.CODEC));

	StreamCodec<RegistryFriendlyByteBuf, ContainerMenu> STREAM_CODEC = Type.STREAM_CODEC.dispatch(ContainerMenu::getType, Type::streamCodec);

	Type<?> getType();

	MenuConstructor constructor(Container container);

	int columns();

	int rows();

	default int size() {
		return columns() * rows();
	}

	interface Type<C extends ContainerMenu> {

		FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.CONTAINER_MENU_TYPE, Kaya.GLOBAL_ALIASES);

		Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

		MapCodec<C> mapCodec();

		StreamCodec<RegistryFriendlyByteBuf, C> streamCodec();

	}

}
