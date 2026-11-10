package io.github.eggohito.kaya.util.services;

import io.github.eggohito.kaya.util.services.manager.KayaServerManagers;
import io.github.eggohito.kaya.util.services.manager.Managers;

public final class KayaServices {

	public static final Managers MANAGERS = ServiceHelper.loadOptionally(Managers.class).orElseGet(KayaServerManagers::new);

}
