package io.github.eggohito.kaya.network;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.network.packet.HandshakePacket;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.network.chat.Component;

import java.util.NoSuchElementException;

public final class KayaServerboundPacketListener {

	public static void init() {

		if (Kaya.performStandaloneHandshake()) {
			HandshakeTask.enableFor(Kaya.MOD_NAMESPACE);
		}

		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.addPhaseOrdering(HandshakeTask.PHASE, Event.DEFAULT_PHASE);
		ServerConfigurationNetworking.registerGlobalReceiver(HandshakePacket.TYPE, KayaServerboundPacketListener::handleHandshakeReply);

	}

	//  TODO:   either avoid using Fabric's version class and use own protocol logic or wait for
	//          https://github.com/FabricMC/fabric-api/pull/4011 to be merged to Fabric API
	private static void handleHandshakeReply(HandshakePacket payload, ServerConfigurationNetworking.Context context) {

		String modId = payload.modId();

		try {

			ModContainer mod = FabricLoader.getInstance()
				.getModContainer(modId)
				.orElseThrow();

			Version serverVersion = mod.getMetadata().getVersion();
			Version clientVersion = Version.parse(payload.modVersion());

			if (serverVersion.equals(clientVersion)) {
				context.networkHandler().completeTask(HandshakeTask.TYPE);
			}

			else {
				context.networkHandler().disconnect(Component.literal("This server is running a different version of '" + modId + "' (" + serverVersion + "), which is incompatible with the one you have installed! (" + clientVersion + ")"));
			}

		}

		catch (VersionParsingException e) {
			context.networkHandler().disconnect(Component.literal("The version of '" + modId + "' you have installed is invalid!"));
		}

		catch (NoSuchElementException ignored) {
			context.networkHandler().disconnect(payload.createMissingModComponent());
		}

	}

}
