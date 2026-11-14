package io.github.eggohito.kaya.power.global;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.registry.tag.KayaTagLikes;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.tag.TagLike;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public record GlobalPowerSet(TagLike<EntityType<?>> entityTypes, TagLike<PowerHolder<?>> powers) implements ContextValidatable {

	public static final ResourceLocation POWER_SOURCE = Kaya.id("global");

	@Override
	public void validate(ContextValidator validator) {
		entityTypes().validate(validator.forChild(".entity_types"));
		powers().validate(validator.forChild(".powers"));
	}

	public boolean doesApply(Entity entity) {
		return entityTypes().isEmpty()
			|| entityTypes().contains(entity.getType());
	}

	public record Template(TagLike.File<EntityType<?>> entityTypes, TagLike.File<PowerHolder<?>> powers) implements ContextValidatable {

		public static final Codec<Template> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			KayaTagLikes.ENTITY_TYPE.fileCodec().optionalFieldOf("entity_types", TagLike.File.empty()).forGetter(Template::entityTypes),
			KayaTagLikes.POWER.fileCodec().optionalFieldOf("powers", TagLike.File.empty()).forGetter(Template::powers)
		).apply(instance, Template::new));

		@Override
		public void validate(ContextValidator validator) {
			entityTypes().validate(validator.forChild(".entity_types"));
			powers().validate(validator.forChild(".powers"));
		}

		public GlobalPowerSet toSet() {
			return new GlobalPowerSet(this.entityTypes().values(), this.powers().values());
		}

	}

	public record File(ResourceLocation id, String source, Template template) implements ContextValidatable {

		@Override
		public void validate(ContextValidator validator) {
			template().validate(validator);
		}

		public File merge(File that) {

			if (!this.id().equals(that.id())) {
				throw new IllegalArgumentException("Cannot merge global power set files \"%s\" and \"%s\", as they have different IDs".formatted(this.id(), that.id()));
			}

			List<String> replacedFields = new ObjectArrayList<>();

			TagLike.File<EntityType<?>> entityTypes = KayaTagLikes.ENTITY_TYPE.merge(this.template().entityTypes(), that.template().entityTypes());
			TagLike.File<PowerHolder<?>> powers = KayaTagLikes.POWER.merge(this.template().powers(), that.template().powers());

			if (entityTypes.replace()) {
				replacedFields.add("entity_types");
			}

			if (powers.replace()) {
				replacedFields.add("powers");
			}

			if (!replacedFields.isEmpty()) {
				Kaya.LOGGER.warn(
					"The data of the {} {} of global power set \"{}\" from data pack [{}] has been replaced with data from data pack [{}]!",
					MiscUtil.allKeys(replacedFields),
					replacedFields.size() > 1 ? "fields": "field",
					this.id(),
					this.source(),
					that.source()
				);
			}

			return new File(
				that.id(),
				that.source(),
				new Template(
					entityTypes,
					powers
				)
			);

		}

	}

}
