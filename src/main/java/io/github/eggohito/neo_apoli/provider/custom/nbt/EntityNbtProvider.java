package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.mixin.access.EntityAccessor;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityNbtProvider(EntityProvider entity) implements NbtProvider {

	public static final MapCodec<EntityNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityNbtProvider::entity)
	).apply(instance, EntityNbtProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityNbtProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, EntityNbtProvider::entity,
		EntityNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.ENTITY;
	}

	@Override
	public Optional<Tag> getValue(Context context) {
		return entity()
			.getValue(context.forChild(".entity"))
			.map(EntityNbtProvider::encode);
	}

	@Override
	public void validate(ContextValidator validator) {
		NbtProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

	private static CompoundTag encode(Entity entity) {

		CompoundTag tag = new CompoundTag();
		String id = ((EntityAccessor) entity).callGetEncodeId();

		if (id != null) {
			tag.putString("id", id);
		}

		return entity.saveWithoutId(tag);

	}

}
