package io.github.eggohito.kaya.registry.tag;

import io.github.eggohito.kaya.action.ActionHolder;
import io.github.eggohito.kaya.action.manager.ActionManager;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.util.tag.TagLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public final class KayaTagLikes {

	public static final TagLike.Source<EntityType<?>> ENTITY_TYPE = TagLike.Source.fromRegistry(BuiltInRegistries.ENTITY_TYPE);

	public static final TagLike.Source<PowerHolder<?>> POWER = TagLike.Source.fromManager(PowerManager::getInstance, PowerIdentifier::of);
	public static final TagLike.Source<ActionHolder<?>> ACTION = TagLike.Source.fromManager(ActionManager::getInstance);

}
