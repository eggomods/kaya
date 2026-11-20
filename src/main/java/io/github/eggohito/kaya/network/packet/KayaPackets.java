package io.github.eggohito.kaya.network.packet;

import io.github.eggohito.kaya.network.packet.clientbound.*;
import io.github.eggohito.kaya.network.packet.serverbound.ServerboundUpdateKeyStatesPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class KayaPackets {

	public static void registerAll() {

		PayloadTypeRegistry.configurationC2S().register(HandshakePacket.TYPE, HandshakePacket.CODEC);
		PayloadTypeRegistry.configurationS2C().register(HandshakePacket.TYPE, HandshakePacket.CODEC);

		PayloadTypeRegistry.playS2C().register(ClientboundUpdateActionsPacket.TYPE, ClientboundUpdateActionsPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientboundUpdateConditionsPacket.TYPE, ClientboundUpdateConditionsPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientboundUpdatePowersPacket.TYPE,  ClientboundUpdatePowersPacket.CODEC);

		PayloadTypeRegistry.playS2C().register(ClientboundClearCachedLogsPacket.TYPE, ClientboundClearCachedLogsPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientboundUpdateCommandStoragePacket.TYPE, ClientboundUpdateCommandStoragePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientboundUpdatePowerDataPacket.TYPE, ClientboundUpdatePowerDataPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientboundPowerRecipeDisplaysUpdatePacket.TYPE, ClientboundPowerRecipeDisplaysUpdatePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientboundUpdateNestedTagPacket.TYPE, ClientboundUpdateNestedTagPacket.CODEC);

		PayloadTypeRegistry.playC2S().register(ServerboundUpdateKeyStatesPacket.TYPE, ServerboundUpdateKeyStatesPacket.CODEC);

	}

}
