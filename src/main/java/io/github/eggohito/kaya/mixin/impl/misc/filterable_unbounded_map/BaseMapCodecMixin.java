package io.github.eggohito.kaya.mixin.impl.misc.filterable_unbounded_map;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.BaseMapCodec;
import io.github.eggohito.kaya.duck.FilteredCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseMapCodec.class)
public interface BaseMapCodecMixin<K, V> {

	@ModifyExpressionValue(method = "lambda$decode$3", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", ordinal = 0))
	default DataResult<K> filterIgnoredKeys(DataResult<K> original, @Cancellable CallbackInfoReturnable<DataResult<Unit>> cir, @Local(argsOnly = true) DataResult<Unit> identity) {

		//noinspection unchecked
		FilteredCodec<K> filterableCodec = FilteredCodec.fromBaseMap((BaseMapCodec<K, V>) this);
		K key = original.result().orElse(null);

		if (filterableCodec != null && key != null && !filterableCodec.kaya$getFilter().test(key)) {
			cir.setReturnValue(identity);
		}

		return original;

	}

}
