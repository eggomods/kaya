package io.github.eggohito.neo_apoli.key.manager;

import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.util.manager.NeoApoliManagers;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public interface KeyStateManager {

	ResourceLocation ID = NeoApoli.id("manager/key_state");

	Supplier<KeyStateManager> DEFERRED_INSTANCE = NeoApoliManagers.deferredPhysicalSideSpecific(KeyStateManager.class, ServerKeyStateManager::new);

	Optional<KeyState> getCurrentState(UUID uuid, String key);

	Optional<KeyState> getPreviousState(UUID uuid, String key);

	@ApiStatus.Internal
	void init();

	static KeyStateManager getInstance() {
		return DEFERRED_INSTANCE.get();
	}

}
