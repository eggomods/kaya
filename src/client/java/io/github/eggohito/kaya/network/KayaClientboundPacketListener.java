package io.github.eggohito.kaya.network;

import io.github.eggohito.kaya.duck.internal.CommandStorageHolder;
import io.github.eggohito.kaya.duck.internal.PowerRecipeDisplayHolder;
import io.github.eggohito.kaya.network.packet.HandshakePacket;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundCommandStorageUpdatePacket;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundPowerRecipeDisplaysUpdatePacket;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundUpdatePowerDataPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.NoSuchElementException;

public final class KayaClientboundPacketListener {

	public static void init() {

		ClientConfigurationNetworking.registerGlobalReceiver(HandshakePacket.TYPE, KayaClientboundPacketListener::sendHandshakeReply);

		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(ClientboundCommandStorageUpdatePacket.TYPE, (payload, context) -> payload.handle((CommandStorageHolder) context.client()));
			ClientPlayNetworking.registerReceiver(ClientboundUpdatePowerDataPacket.TYPE, (payload, context) -> payload.handle(context.player().level()));
			ClientPlayNetworking.registerReceiver(ClientboundPowerRecipeDisplaysUpdatePacket.TYPE, (payload, context) -> payload.handle((PowerRecipeDisplayHolder) context.client()));
		});

	}

	private static void sendHandshakeReply(HandshakePacket payload, ClientConfigurationNetworking.Context context) {

		try {

			String modId = payload.modId();
			String version = FabricLoader.getInstance()
				.getModContainer(modId)
				.map(ModContainer::getMetadata)
				.map(metadata -> metadata.getVersion().getFriendlyString())
				.orElseThrow();

			context.responseSender().sendPacket(new HandshakePacket(modId, version));

		}

		catch (NoSuchElementException ignored) {
			context.responseSender().disconnect(payload.createMissingModComponent());
		}

	}

}
