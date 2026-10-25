package io.github.eggohito.neo_apoli.client.mixin.impl.command.storage_holder;

import io.github.eggohito.neo_apoli.client.duck.internal.ClientCommandStorageHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements ClientCommandStorageHolder {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Override
	public CompoundTag neo_apoli$getStorage(ResourceLocation id) {
		return this.neo_apoli$getStorageHolder().neo_apoli$getStorage(id);
	}

	@Override
	public boolean neo_apoli$contains(ResourceLocation id) {
		return this.neo_apoli$getStorageHolder().neo_apoli$contains(id);
	}

	@Override
	public void neo_apoli$setStorage(ResourceLocation id, CompoundTag nbt) {
		this.neo_apoli$getStorageHolder().neo_apoli$setStorage(id, nbt);
	}

	@Override
	public void neo_apoli$clear() {
		this.neo_apoli$getStorageHolder().neo_apoli$clear();
	}

	@Unique
	private ClientCommandStorageHolder neo_apoli$getStorageHolder() {
		return (ClientCommandStorageHolder) this.minecraft;
	}

}
