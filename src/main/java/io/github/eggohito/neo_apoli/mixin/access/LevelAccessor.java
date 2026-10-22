package io.github.eggohito.neo_apoli.mixin.access;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Level.class)
public interface LevelAccessor {

	@Invoker
	boolean callCanHaveWeather();

}
