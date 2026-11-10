package io.github.eggohito.kaya.event;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.modifier.Modifier;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ModifierOrderCallback {

	ResourceLocation PHASE = Kaya.id("modifier_order");

	Event<ModifierOrderCallback> EVENT = EventFactory.createArrayBacked(
		ModifierOrderCallback.class,
		callbacks -> orderer -> {

			for (var callback : callbacks) {
				callback.order(orderer);
			}

		}
	);

	void order(Modifier.Orderer orderer);

}
