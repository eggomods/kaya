package io.github.eggohito.neo_apoli.client.key.manager;

import io.github.eggohito.neo_apoli.event.KeyStateEvents;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.key.manager.ServerKeyStateManager;
import io.github.eggohito.neo_apoli.network.packet.serverbound.ServerboundUpdateKeyStatesPacket;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public final class ClientKeyStateManager extends ServerKeyStateManager {

	private final Object2BooleanMap<UUID> init = new Object2BooleanOpenHashMap<>();

	@Override
	public void init() {

		super.init();

		ClientTickEvents.END_CLIENT_TICK.register(ID, this::tick);
		ClientPlayConnectionEvents.DISCONNECT.register(ID, (handler, client) -> this.disconnect());

	}

	private void disconnect() {

		this.previousStates.remove();
		this.currentStates.remove();

		this.init.clear();

	}

	private void tick(Minecraft client) {

		LocalPlayer player = client.player;
		ClientLevel level = client.level;

		if (player == null || level == null) {
			return;
		}

		UUID uuid = player.getUUID();
		Object2BooleanMap<String> updates = new Object2BooleanOpenHashMap<>();

		Map<String, KeyState> previous = previousStates.get().computeIfAbsent(uuid, k -> new Object2ObjectLinkedOpenHashMap<>());
		Map<String, KeyState> current = currentStates.get().computeIfAbsent(uuid, k -> new Object2ObjectLinkedOpenHashMap<>());

		for (var keyMapping : client.options.keyMappings) {

			String id = keyMapping.getName();
			boolean pressed = keyMapping.isDown();

			KeyState previousState = previous.computeIfAbsent(id, KeyState::new);
			KeyState currentState = previousState;

			boolean changed = previousState.pressed() != pressed;

			if (changed) {
				currentState = new KeyState(id, pressed ? level.getGameTime() : 0);
			}

			if (!init.containsKey(uuid) || changed) {
				updates.put(id, pressed);
			}

			current.put(id, currentState);

			if (changed) {

				if (pressed) {
					KeyStateEvents.PRESSED.invoker().onPressed(player, currentState);
				}

				else {
					KeyStateEvents.RELEASED.invoker().onReleased(player, currentState);
				}

			}

			if (currentState.pressed()) {
				KeyStateEvents.HELD.invoker().onHeld(player, previousState, currentState);
			}

		}

		previous.putAll(current);
		send(updates);

		init.putIfAbsent(uuid, true);

	}

	private void send(Object2BooleanMap<String> updates) {

		if (!updates.isEmpty()) {
			ClientPlayNetworking.send(new ServerboundUpdateKeyStatesPacket(updates));
		}

	}

}
