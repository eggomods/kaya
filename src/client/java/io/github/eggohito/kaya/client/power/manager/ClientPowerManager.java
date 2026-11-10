package io.github.eggohito.kaya.client.power.manager;

import com.google.common.collect.ImmutableMap;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundUpdatePowersPacket;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.power.manager.ServerPowerManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ClientPowerManager extends ServerPowerManager {

	@Override
	public void init() {

		super.init();

		ClientPlayNetworking.registerGlobalReceiver(ClientboundUpdatePowersPacket.TYPE, (payload, context) -> this.receive(payload));
		ClientPlayConnectionEvents.DISCONNECT.register(PowerManager.ID, (handler, client) -> this.clear());

	}

	private void receive(ClientboundUpdatePowersPacket payload) {

		ImmutableMap.Builder<PowerIdentifier, PowerHolder<?>> builder = ImmutableMap.builder();
		payload.powers().values().forEach(powerHolder -> this.register(powerHolder, builder::put));

		this.contents = builder.build();
		this.tags = ImmutableMap.copyOf(payload.tags());

	}

	private void clear() {
		this.contents = ImmutableMap.of();
		this.tags = ImmutableMap.of();
	}

}
