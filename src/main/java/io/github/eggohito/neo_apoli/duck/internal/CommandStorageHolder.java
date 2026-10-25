package io.github.eggohito.neo_apoli.duck.internal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface CommandStorageHolder {

	CompoundTag neo_apoli$getStorage(ResourceLocation id);

	boolean neo_apoli$contains(ResourceLocation id);

	void neo_apoli$setStorage(ResourceLocation id, CompoundTag nbt);

}
