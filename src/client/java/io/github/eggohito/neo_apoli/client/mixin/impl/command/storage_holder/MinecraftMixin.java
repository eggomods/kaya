package io.github.eggohito.neo_apoli.client.mixin.impl.command.storage_holder;

import io.github.eggohito.neo_apoli.client.duck.internal.ClientCommandStorageHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements ClientCommandStorageHolder {

	@Unique
	private final Object2ObjectOpenHashMap<ResourceLocation, CompoundTag> neo_apoli$commandStorageCache = new Object2ObjectOpenHashMap<>();

	@Override
	public CompoundTag neo_apoli$getStorage(ResourceLocation id) {
		return neo_apoli$commandStorageCache.getOrDefault(id, new CompoundTag());
	}

	@Override
	public boolean neo_apoli$contains(ResourceLocation id) {
		return neo_apoli$commandStorageCache.containsKey(id);
	}

	@Override
	public void neo_apoli$setStorage(ResourceLocation id, CompoundTag nbt) {

		if (nbt.isEmpty()) {
			neo_apoli$commandStorageCache.remove(id);
		}

		else {
			neo_apoli$commandStorageCache.put(id, nbt);
		}

	}

	@Override
	public void neo_apoli$clear() {
		this.neo_apoli$commandStorageCache.clear();
		this.neo_apoli$commandStorageCache.trim();
	}

}
