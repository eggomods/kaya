package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.hud.element.HudElement;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record HudRenderPower(List<HudElement> hudElements, Properties properties) implements Power {

	public static final MapCodec<HudRenderPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(HudElement.CODEC.listOf()).fieldOf("hud_elements").forGetter(HudRenderPower::hudElements),
		Power.propertiesCodecBuilder(false)
	).apply(instance, HudRenderPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, HudRenderPower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(ObjectArrayList::new, HudElement.STREAM_CODEC), HudRenderPower::hudElements,
		Properties.STREAM_CODEC, HudRenderPower::properties,
		HudRenderPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.HUD_RENDER;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		Power.super.validate(validator);
		ContextValidatable.validate(hudElements(), validator, index -> ".hud_elements[" + index + "]");
	}

	public static class Instance extends Power.Instance<HudRenderPower> {

		protected Instance(@NotNull HudRenderPower power) {
			super(power);
		}

		public List<HudElement> hudElements() {
			return power.hudElements();
		}

	}

}
