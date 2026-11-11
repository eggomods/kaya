package io.github.eggohito.kaya.network;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.network.packet.HandshakePacket;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record HandshakeTask(String modId, String modVersion) implements ConfigurationTask {

	public static final ResourceLocation PHASE = Kaya.id("handshake");
	public static final Type TYPE = new Type(PHASE.toString());

	@Override
	public @NotNull Type type() {
		return TYPE;
	}

	@Override
	public void start(Consumer<Packet<?>> task) {
		task.accept(ServerConfigurationNetworking.createS2CPacket(new HandshakePacket(this.modId(), this.modVersion())));
	}

	public static void enableFor(String modId) {

		String modVersion = FabricLoader.getInstance()
			.getModContainer(modId)
			.map(ModContainer::getMetadata)
			.map(metadata -> metadata.getVersion().getFriendlyString())
			.orElse(null);

		if (modVersion != null) {
			ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register(PHASE, (handler, server) -> request(modId, modVersion, handler));
		}

		else {
			throw new IllegalArgumentException("Tried enabling version handshake for mod \"" + modId + "\", which does not exist!");
		}

	}

	public static Component createMissingModComponent(String modId, String modVersion) {
		return Component.literal("This server requires you to install '" + modId + "' (" + modVersion + ") to play!");
	}

	private static void request(String modId, String modVersion, ServerConfigurationPacketListenerImpl handler) {

		if (ServerConfigurationNetworking.canSend(handler, HandshakePacket.TYPE)) {
			handler.addTask(new HandshakeTask(modId, modVersion));
		}

		else {
			handler.disconnect(createMissingModComponent(modId, modVersion));
		}

	}

}
