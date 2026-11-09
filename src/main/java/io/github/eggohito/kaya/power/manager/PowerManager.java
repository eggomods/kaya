package io.github.eggohito.kaya.power.manager;

import com.mojang.serialization.DataResult;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.power.custom.MultiplePower;
import io.github.eggohito.kaya.util.manager.ContentAndTagManager;
import io.github.eggohito.kaya.util.manager.KayaManagers;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public interface PowerManager extends ContentAndTagManager<PowerIdentifier, PowerHolder<?>> {

	ResourceLocation ID = Kaya.id("manager/power");

	Supplier<PowerManager> DEFERRED_INSTANCE = KayaManagers.deferredPhysicalSideSpecific(PowerManager.class, ServerPowerManager::new);

	@Override
	default DataResult<PowerHolder<?>> getAsResult(PowerIdentifier key) {
		return this.getAsResult(key, k -> "Unknown " + k.asDisplayString(false));
	}

	@Override
	default DataResult<PowerIdentifier> getKeyAsResult(PowerHolder<?> value) {
		return this.getKeyAsResult(value, v -> "Unregistered power holder: " + v);
	}

	@Override
	default DataResult<List<PowerHolder<?>>> getTagAsResult(ResourceLocation id) {
		return this.getTagAsResult(id, i -> "Unknown power tag: \"" + i + "\"");
	}

	default DataResult<PowerIdentifier> getKeyAsResult(Power power) {

		for (var candidate : values()) {

			if (candidate.value() == power) {
				return DataResult.success(candidate.id());
			}

		}

		return DataResult.error(() -> "Unregistered power: " + power);

	}

	default PowerIdentifier getKey(Power power) {
		return this.getKeyAsResult(power).getOrThrow();
	}

	@ApiStatus.Internal
	void init();

	static PowerManager getInstance() {
		return DEFERRED_INSTANCE.get();
	}

	static void handleSelfAndSubPowers(PowerHolder<?> holder, BiConsumer<PowerIdentifier, PowerHolder<?>> handler) {

		handler.accept(holder.id(), holder);

		if (holder.value() instanceof MultiplePower multiplePower) {
			multiplePower.subPowers().forEach(subPower -> handleSelfAndSubPowers(MultiplePower.validateNonRecursiveMultiple(subPower), handler));
		}

	}

}
