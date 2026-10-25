package io.github.eggohito.neo_apoli.mixin.access;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.CommandStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(CommandStorage.class)
public interface CommandStorageAccessor {

	@Accessor
	Map<String, CommandStorage.Container> getNamespaces();

	@Nullable
	@Invoker
	CommandStorage.Container callGetContainer(String namespace);

	@Mixin(CommandStorage.Container.class)
	interface ContainerAccessor {

		@Accessor("storage")
		Map<String, CompoundTag> getData();

	}

}
