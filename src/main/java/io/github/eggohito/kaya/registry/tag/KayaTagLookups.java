package io.github.eggohito.kaya.registry.tag;

import io.github.eggohito.kaya.action.ActionHolder;
import io.github.eggohito.kaya.action.manager.ActionManager;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.util.tag.TagLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public final class KayaTagLookups {

	public static final TagLookup<EntityType<?>> ENTITY_TYPE = TagLookup.fromRegistry(BuiltInRegistries.ENTITY_TYPE);

	public static final TagLookup<PowerHolder<?>> POWER = TagLookup.fromManager(PowerManager::getInstance, PowerIdentifier::of);
	public static final TagLookup<ActionHolder<?>> ACTION = TagLookup.fromManager(ActionManager::getInstance);

}
