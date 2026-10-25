package io.github.eggohito.neo_apoli.util.services;

import java.util.ServiceLoader;

public final class NeoApoliServices {

	public static <T> T load(Class<T> serviceClass) {
		return ServiceLoader.load(serviceClass, NeoApoliServices.class.getClassLoader())
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Couldn't load service for " + serviceClass.getName()));
	}

}
