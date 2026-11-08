package io.github.eggohito.kaya.context;

import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import net.minecraft.world.entity.Entity;

public final class ContextHelper {

	public static Context.Builder builderForEntity(Entity entity) {
		return new Context.Builder().withRequired(KayaContextParameters.THIS_ENTITY, entity);
	}

	public static Context forEntity(Entity entity) {
		return builderForEntity(entity).build(entity.level());
	}

}
