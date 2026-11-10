package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.duck.internal.EntityCache;
import io.github.eggohito.kaya.event.PowerModifyEvents;
import io.github.eggohito.kaya.modifier.Modifier;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.RegistryUtil;
import io.github.eggohito.kaya.util.Reporter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public record ModifyAttributePower(Holder<Attribute> attribute, List<Modifier> modifiers, Properties properties) implements Power {

	public static final MapCodec<ModifyAttributePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Attribute.CODEC.fieldOf("attribute").forGetter(ModifyAttributePower::attribute),
		ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyAttributePower::modifiers),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyAttributePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyAttributePower> STREAM_CODEC = StreamCodec.composite(
		Attribute.STREAM_CODEC, ModifyAttributePower::attribute,
		Modifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ModifyAttributePower::modifiers,
		Properties.STREAM_CODEC, ModifyAttributePower::properties,
		ModifyAttributePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_ATTRIBUTE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static final class Instance extends Power.Instance<ModifyAttributePower> {

		Instance(@NotNull ModifyAttributePower power) {
			super(power);
		}

		public void addOperations(Context context, Consumer<Modifier.Operation> adder) {
			MiscUtil.iterateList(
				power.modifiers(),
				(index, modifier) -> adder.accept(modifier.asOperation(context.forChild(".modifiers[" + index + "]")))
			);
		}

		public boolean matches(Holder<Attribute> attribute) {
			//noinspection deprecation
			return power.attribute().is(attribute);
		}

	}

	public static double modify(AttributeInstance attributeInstance, double totalValue) {

		tryModify:
		if (attributeInstance instanceof EntityCache entityCache) {

			Entity entity = entityCache.kaya$getEntity();
			List<Modifier.Operation> operations = new ObjectArrayList<>();

			for (var instance : Powers.getInstances(entity, Instance.class, instance -> instance.matches(attributeInstance.getAttribute()))) {

				Context context = instance.createHolderContext(entity);

				if (instance.isActive(context)) {
					instance.addOperations(context, operations::add);
				}

			}

			if (entity == null || operations.isEmpty()) {
				break tryModify;
			}

			Context simpleContext = new Context.Builder()
				.withReporter(new Reporter("{\"" + RegistryUtil.getId(BuiltInRegistries.ATTRIBUTE, attributeInstance.getAttribute().value()) + "\"}"))
				.withRequired(KayaContextParameters.THIS_ENTITY, entity)
				.build(entity.level());

			MiscUtil.iterate(
				attributeInstance.getModifiers(),
				(index, attributeModifier) -> operations.add(Modifier
					.fromVanilla(attributeModifier)
					.asOperation(simpleContext.forChild(".modifiers[" + index + "]")))
			);

			PowerModifyEvents.NUMBER.invoker().beforeModified(KayaPowerTypes.MODIFY_ATTRIBUTE, operations, attributeInstance.getBaseValue());
			return Modifier.applyAll(operations, attributeInstance.getBaseValue());

		}

		return totalValue;

	}

}
