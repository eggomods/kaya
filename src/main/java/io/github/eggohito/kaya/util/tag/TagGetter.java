package io.github.eggohito.kaya.util.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface TagGetter<T> {

	@Nullable
	Collection<T> tag(ResourceLocation id);

	@Nullable
	default Collection<T> tag(TagKey<T> tag) {
		return this.tag(tag.location());
	}

}
