package io.github.eggohito.neo_apoli.mixin.access;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {

	@Accessor
	boolean isFirstTick();

}
