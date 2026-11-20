package io.github.eggohito.kaya.mixin.impl.misc.server_access;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eggohito.kaya.duck.internal.CommandStorageExtension;
import io.github.eggohito.kaya.mixin.access.CommandStorageAccessor;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundUpdateCommandStoragePacket;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.CommandStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Set;

@Mixin(CommandStorage.class)
public abstract class CommandStorageMixin implements CommandStorageExtension {

	@Shadow
	@Final
	private Map<String, CommandStorage.Container> namespaces;

	@Unique
	private final Set<ResourceLocation> kaya$changed = new ObjectOpenHashSet<>();

	@Override
	public boolean kaya$hasChanged() {
		return !this.kaya$changed.isEmpty();
	}

	@Override
	public void kaya$sync(MinecraftServer server, boolean onlyWhenChanged) {

		if (onlyWhenChanged && this.kaya$hasChanged()) {
			return;
		}

		for (var storage : this.namespaces.entrySet()) {

			String namespace = storage.getKey();
			Map<String, CompoundTag> paths = ((CommandStorageAccessor.ContainerAccessor) storage.getValue()).getData();

			for (var entry : paths.entrySet()) {

				ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, entry.getKey());
				CompoundTag data = entry.getValue();

				if (this.kaya$changed.remove(id) || !onlyWhenChanged) {

					for (var recipient : server.getPlayerList().getPlayers()) {
						ServerPlayNetworking.send(recipient, new ClientboundUpdateCommandStoragePacket(id, data));
					}

				}

			}

		}

	}

	@WrapOperation(method = "set", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/CommandStorage$Container;put(Ljava/lang/String;Lnet/minecraft/nbt/CompoundTag;)V"))
	void trackChangedStatus(CommandStorage.Container container, String path, CompoundTag nbt, Operation<Void> original, @Local(argsOnly = true) ResourceLocation id) {

		original.call(container, path, nbt);

		if (container.isDirty()) {
			this.kaya$changed.add(id);
		}

	}

}
