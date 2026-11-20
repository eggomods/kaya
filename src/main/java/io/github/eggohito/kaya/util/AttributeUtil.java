package io.github.eggohito.kaya.util;

import io.github.eggohito.kaya.mixin.access.LivingEntityAccessor;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.DoubleSupplier;

public final class AttributeUtil {

	public static double getValue(Entity entity, Holder<Attribute> attribute, DoubleSupplier defaultValue) {

		if (!(entity instanceof LivingEntity livingEntity)) {
			return defaultValue.getAsDouble();
		}

		AttributeMap attributeMap = livingEntity.getAttributes();
		float value = (float) attributeMap.getValue(attribute);

		//  additional sanitation
		if (attribute == Attributes.SCALE) {
			value = ((LivingEntityAccessor) livingEntity).callSanitizeScale(value);
		}

		return value;

	}

}
