package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.tag.manager.NestedTagManager;
import io.github.eggohito.kaya.util.RegistryUtil;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ModifyEntityTypeTagPower(TagKey<EntityType<?>> tag, Properties properties) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyEntityTypeTagPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.hashedCodec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(ModifyEntityTypeTagPower::tag),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyEntityTypeTagPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyEntityTypeTagPower> STREAM_CODEC = StreamCodec.composite(
		TagKey.streamCodec(Registries.ENTITY_TYPE), ModifyEntityTypeTagPower::tag,
		Properties.STREAM_CODEC, ModifyEntityTypeTagPower::properties,
		ModifyEntityTypeTagPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_ENTITY_TYPE_TAG;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		RegistryUtil.validateTag(validator.forChild(".tag"), this.tag());
	}

	public static class Instance extends Power.Instance<ModifyEntityTypeTagPower> {

		protected Instance(@NotNull ModifyEntityTypeTagPower power) {
			super(power);
		}

		public boolean doesApply(TagKey<EntityType<?>> tag) {

			if (Objects.equals(power.tag(), tag)) {
				return true;
			}

			else {

				for (var nestedTag : NestedTagManager.getInstance().getOrCreate(Registries.ENTITY_TYPE).getOrEmpty(tag)) {

					if (this.doesApply(nestedTag)) {
						return true;
					}

				}

				return false;

			}

		}

	}

	public static boolean modify(Entity entity, HolderSet<EntityType<?>> directTag) {
		return directTag.unwrapKey()
			.map(tag -> modify(entity, tag))
			.orElse(false);
	}

	public static boolean modify(Entity entity, TagKey<EntityType<?>> tag) {

		for (var instance : Powers.getInstances(entity, Instance.class)) {

			Context context = instance.createHolderContext(entity);

			try {

				if (VISITOR.push(instance) && instance.isActive(context) && instance.doesApply(tag)) {
					return true;
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return false;

	}

}
