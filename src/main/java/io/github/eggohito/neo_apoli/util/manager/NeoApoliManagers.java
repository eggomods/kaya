package io.github.eggohito.neo_apoli.util.manager;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import io.github.eggohito.neo_apoli.util.services.NeoApoliServices;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.util.function.Supplier;

public final class NeoApoliManagers {

	private static final Logger LOGGER = LogUtils.getLogger();

	public static <M> M physicalSideSpecific(Class<M> forClient, Supplier<M> forServer) {
		return switch (FabricLoader.getInstance().getEnvironmentType()) {
			case CLIENT -> {
				LOGGER.debug("Client detected; attempting to load service for '{}'", forClient.getName());
				yield NeoApoliServices.load(forClient);
			}
			case SERVER -> {

				M instance = forServer.get();
				LOGGER.debug("Dedicated server detected; instantiating '{}'", instance.getClass().getName());

				yield instance;

			}
		};
	}

	public static <M> Supplier<M> deferredPhysicalSideSpecific(Class<M> forClient, Supplier<M> forServer) {
		return Suppliers.memoize(() -> physicalSideSpecific(forClient, forServer));
	}

}
