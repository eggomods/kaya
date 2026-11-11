package io.github.eggohito.kaya.key.manager;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.key.KeyState;
import io.github.eggohito.kaya.manager.Manager;
import io.github.eggohito.kaya.registry.manager.KayaManagerKeys;
import io.github.eggohito.kaya.util.services.KayaServices;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.UUID;

@ApiStatus.NonExtendable
public interface KeyStateManager extends Manager {

	ResourceLocation ID = Kaya.id("manager/key_state");

	Optional<KeyState> getCurrentState(UUID uuid, String key);

	Optional<KeyState> getPreviousState(UUID uuid, String key);

	@ApiStatus.Internal
	void init();

	static KeyStateManager getInstance() {
		return KayaServices.MANAGERS.getOrThrow(KayaManagerKeys.KEY_STATE);
	}

}
