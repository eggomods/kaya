package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.hud.element.HudElement;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.CooldownPower;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record CooldownStandalonePower(HudElement hudElement, IntProvider cooldown, Properties properties) implements CooldownPower {

	public static final MapCodec<CooldownStandalonePower> CODEC = RecordCodecBuilder.mapCodec(instance -> CooldownPower
		.addFields(instance)
		.and(Power.propertiesCodecBuilder(false))
		.apply(instance, CooldownStandalonePower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, CooldownStandalonePower> STREAM_CODEC = StreamCodec.composite(
		HudElement.STREAM_CODEC, CooldownStandalonePower::hudElement,
		IntProvider.STREAM_CODEC, CooldownStandalonePower::cooldown,
		Properties.STREAM_CODEC, CooldownStandalonePower::properties,
		CooldownStandalonePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.COOLDOWN;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new CooldownPower.Instance<>(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		CooldownPower.super.validate(validator);
		hudElement().validate(validator.forChild(".hud_element"));
		cooldown().validate(validator.forChild(".cooldown"));
	}

}
