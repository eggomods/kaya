package io.github.eggohito.kaya.mixin.impl.misc.filterable_unbounded_map;

import com.google.common.base.Predicates;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import io.github.eggohito.kaya.duck.FilteredCodec;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Predicate;

@Mixin(UnboundedMapCodec.class)
public abstract class UnboundedMapCodecMixin<K, V> implements FilteredCodec<K> {

	@Unique
	Predicate<K> kaya$filter = Predicates.alwaysTrue();

	@Override
	public Predicate<K> kaya$getFilter() {
		return this.kaya$filter;
	}

	@Override
	public void kaya$setFilter(@NotNull Predicate<K> filter) {
		this.kaya$filter = filter;
	}

}
