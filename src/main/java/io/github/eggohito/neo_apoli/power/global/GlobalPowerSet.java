package io.github.eggohito.neo_apoli.power.global;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.PowerHolder;
import io.github.eggohito.neo_apoli.power.manager.PowerManager;
import io.github.eggohito.neo_apoli.util.tag.TagLike;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public record GlobalPowerSet(TagLike<EntityType<?>> entityTypes, TagLike<PowerHolder<?>> powers) implements ContextUser {

	public static final ResourceLocation POWER_SOURCE = NeoApoli.id("global");

	@Override
	public void validate(ContextValidator validator) {
		entityTypes().validate(validator.forChild(".entity_types"));
		powers().validate(validator.forChild(".powers"));
	}

	public boolean doesApply(Entity entity) {
		return entityTypes().isEmpty()
			|| entityTypes().contains(entity.getType());
	}

	public record File(List<TagEntry> entityTypes, List<TagEntry> powers, boolean replace) {

		public static final MapCodec<File> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			TagEntry.CODEC.listOf().optionalFieldOf("entity_types", List.of()).forGetter(File::entityTypes),
			TagEntry.CODEC.listOf().optionalFieldOf("powers", List.of()).forGetter(File::powers),
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(File::replace)
		).apply(instance, File::new));

	}

	public record SourcedFile(List<TagEntry> entityTypes, List<TagEntry> powers, String source, boolean replace) {

		public SourcedFile(File file, String source) {
			this(file.entityTypes(), file.powers(), source, file.replace());
		}

		public SourcedFile merge(SourcedFile that, Runnable onMerged) {

			List<TagEntry> entityTypes = new ObjectArrayList<>();
			List<TagEntry> powers = new ObjectArrayList<>();

			if (that.replace()) {
				onMerged.run();
			}

			else {
				entityTypes.addAll(this.entityTypes());
				powers.addAll(this.powers());
			}

			entityTypes.addAll(that.entityTypes());
			powers.addAll(that.powers());

			return new SourcedFile(
				entityTypes,
				powers,
				that.source(),
				that.replace()
			);

		}

		public GlobalPowerSet asSet() {
			return new GlobalPowerSet(
				TagLike.ofRegistry(entityTypes(), BuiltInRegistries.ENTITY_TYPE),
				TagLike.of(powers(), PowerManager.TAG_LOOKUP)
			);
		}

	}

}
