package io.github.eggohito.neo_apoli.mixin.access;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

	@Accessor
	boolean isFirstTick();

	@Nullable
	@Invoker
	String callGetEncodeId();

}
