package io.github.eggohito.kaya.mixin.impl.command.storage_holder;

import io.github.eggohito.kaya.duck.internal.CommandStorageHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements CommandStorageHolder {

	@Shadow
	@NotNull
	public abstract MinecraftServer getServer();

	@Override
	public CompoundTag kaya$getStorage(ResourceLocation id) {
		return this.kaya$asStorageHolder().kaya$getStorage(id);
	}

	@Override
	public boolean kaya$contains(ResourceLocation id) {
		return this.kaya$asStorageHolder().kaya$contains(id);
	}

	@Override
	public void kaya$setStorage(ResourceLocation id, CompoundTag nbt) {
		this.kaya$asStorageHolder().kaya$setStorage(id, nbt);
	}

	@Unique
	private CommandStorageHolder kaya$asStorageHolder() {
		return (CommandStorageHolder) this.getServer();
	}

}
