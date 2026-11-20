package io.github.eggohito.kaya.mixin.impl.command.storage_holder;

import io.github.eggohito.kaya.duck.internal.CommandStorageHolder;
import io.github.eggohito.kaya.mixin.access.CommandStorageAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.CommandStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements CommandStorageHolder {

	@Shadow
	public abstract CommandStorage getCommandStorage();

	@Override
	public CompoundTag kaya$getStorage(ResourceLocation id) {
		return this.getCommandStorage().get(id);
	}

	@Override
	public boolean kaya$contains(ResourceLocation id) {
		CommandStorage.Container container = ((CommandStorageAccessor) this.getCommandStorage()).callGetContainer(id.getNamespace());
		return container != null
			&& ((CommandStorageAccessor.ContainerAccessor) container).getData().get(id.getPath()) != null;
	}

	@Override
	public void kaya$setStorage(ResourceLocation id, CompoundTag nbt) {
		this.getCommandStorage().set(id, nbt);
	}

}
