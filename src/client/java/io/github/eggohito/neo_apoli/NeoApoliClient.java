package io.github.eggohito.neo_apoli;

import io.github.eggohito.neo_apoli.client.duck.internal.ClientCommandStorageHolder;
import io.github.eggohito.neo_apoli.client.integration.ClientConfigIntegrations;
import io.github.eggohito.neo_apoli.client.integration.PowerClientIntegrations;
import io.github.eggohito.neo_apoli.client.util.registry.NeoApoliAtlases;
import io.github.eggohito.neo_apoli.client.util.registry.NeoApoliHudElementRenderers;
import io.github.eggohito.neo_apoli.duck.internal.PowerRecipeDisplayHolder;
import io.github.eggohito.neo_apoli.network.NeoApoliClientboundPacketListener;
import io.github.eggohito.neo_apoli.network.packet.clientbound.ClientboundClearCachedLogsPacket;
import io.github.eggohito.neo_apoli.registry.NeoApoliColorResolvers;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;

public class NeoApoliClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		NeoApoliClientboundPacketListener.init();

		PowerClientIntegrations.registerAll();
		ClientConfigIntegrations.init();

		NeoApoliAtlases.registerAll();
		NeoApoliColorResolvers.registerAll(ColorResolverRegistry::register);

		NeoApoliHudElementRenderers.registerAll();

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			((ClientCommandStorageHolder) client).neo_apoli$clear();
			((PowerRecipeDisplayHolder) client).neo_apoli$setPowerIdsByIndex(new Int2ObjectOpenHashMap<>());
		});

		ClientPlayNetworking.registerGlobalReceiver(ClientboundClearCachedLogsPacket.TYPE, (payload, context) -> clearCachedLogs());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clearCachedLogs());

	}

	public static void clearCachedLogs() {
		NeoApoli.CACHED_LOGS.clear();
	}

}
