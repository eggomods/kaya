package io.github.eggohito.kaya.util.services;

import java.util.Optional;
import java.util.ServiceLoader;

public final class ServiceHelper {

	public static <T> Optional<T> loadOptionally(Class<T> serviceClass) {
		return ServiceLoader.load(serviceClass, KayaServices.class.getClassLoader()).findFirst();
	}

	public static <T> T load(Class<T> serviceClass) {
		return loadOptionally(serviceClass).orElseThrow(() -> new IllegalArgumentException("Couldn't load service for " + serviceClass.getName()));
	}

}
