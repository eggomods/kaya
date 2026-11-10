package io.github.eggohito.kaya.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.event.PowerParsingEvents;
import io.github.eggohito.kaya.power.custom.MultiplePower;
import io.github.eggohito.kaya.util.ComponentUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record PowerHolder<P extends Power>(PowerIdentifier id, P value, Component name, Component description) implements ContextValidatable {

	public static final String ID_KEY = "id";

	public static final MapCodec<PowerHolder<?>> MAP_CODEC = mapCodec(Power.MAP_CODEC);

	public static final Codec<PowerHolder<?>> CODEC = MAP_CODEC.codec();

	public static final StreamCodec<RegistryFriendlyByteBuf, PowerHolder<?>> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, PowerHolder::id,
		Power.STREAM_CODEC, PowerHolder::value,
		PowerHolder::new
	);

	private PowerHolder(PowerIdentifier id, P value, String translationKey) {
		this(id, value, ComponentUtil.translatable(translationKey + ".name", value.name()), ComponentUtil.translatable(translationKey + ".description", value.description()));
	}

	public PowerHolder(PowerIdentifier id, P value) {
		this(id, value, id.createTranslationKey());
	}

	public PowerHolder {

		if (id.isSubPower()) {
			MultiplePower.validateSubPower(value).getOrThrow();
		}

	}

	public boolean isSubPower() {
		return id().isSubPower();
	}

	public boolean isHidden(Context context) {
		return isSubPower()
			|| value().properties().hidden(context);
	}

	@Override
	public void validate(ContextValidator validator) {
		value().validate(validator.withParams(value().getType().parameters()));
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		else if (obj instanceof PowerHolder<?> that) {
			return Objects.equals(this.id(), that.id());
		}

		else {
			return false;
		}

	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id());
	}

	public static MapCodec<PowerHolder<?>> mapCodec(MapCodec<Power> powerCodec) {

		MapCodec<PowerHolder<?>> direct = RecordCodecBuilder.mapCodec(instance -> instance.group(
			PowerIdentifier.CODEC.fieldOf(ID_KEY).forGetter(PowerHolder::id),
			powerCodec.forGetter(PowerHolder::value)
		).apply(instance, PowerHolder::new));

		return direct.mapResult(PowerParsingEvents.RESULT_MAPPER);

	}

}
