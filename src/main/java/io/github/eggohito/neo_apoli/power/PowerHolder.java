package io.github.eggohito.neo_apoli.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.event.PowerParsingEvents;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.util.ComponentUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record PowerHolder<P extends Power>(PowerIdentifier id, P value, Component name, Component description, BooleanProvider hidden) implements ContextValidatable {

	public static final String ID_KEY = "id";

	public static final MapCodec<PowerHolder<?>> MAP_CODEC = mapCodec(Power.MAP_CODEC);
	public static final Codec<PowerHolder<?>> CODEC = MAP_CODEC.codec();

	public static final StreamCodec<RegistryFriendlyByteBuf, PowerHolder<?>> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, PowerHolder::id,
		Power.STREAM_CODEC, PowerHolder::value,
		ComponentSerialization.TRUSTED_STREAM_CODEC, PowerHolder::name,
		ComponentSerialization.TRUSTED_STREAM_CODEC, PowerHolder::description,
		BooleanProvider.STREAM_CODEC, PowerHolder::hidden,
		PowerHolder::new
	);

	public PowerHolder {

		String translationKey = id.createTranslationKey();

		name = ComponentUtil.translatable(translationKey + ".name", name);
		description = ComponentUtil.translatable(translationKey + ".description", description);

	}

	public boolean canBePartiallyParsed() {
		return value().canBePartiallyParsed();
	}

	public boolean isSubPower() {
		return id().isSubPower();
	}

	public boolean isHidden(Context context) {
		return isSubPower()
			|| hidden().getBoolean(context.forChild(".hidden"));
	}

	@Override
	public void validate(ContextValidator validator) {
		value().validate(validator.withParams(value().getType().parameters()));
		hidden().validate(validator.withParams(NeoApoliPowerTypes.DEFAULT_PARAMETERS).forChild(".hidden"));
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

	public static MapCodec<PowerHolder<?>> mapCodecWithId(ResourceLocation id, MapCodec<Power> powerCodec) {

		MapCodec<PowerHolder<?>> direct = RecordCodecBuilder.mapCodec(instance -> instance.group(
			powerCodec.forGetter(PowerHolder::value),
			ComponentSerialization.CODEC.optionalFieldOf("name", Component.empty()).forGetter(PowerHolder::name),
			ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(PowerHolder::description),
			BooleanProvider.CODEC.optionalFieldOf("hidden", new ConstantBooleanProvider(false)).forGetter(PowerHolder::hidden)
		).apply(instance, (power, name, description, hidden) -> new PowerHolder<>(PowerIdentifier.of(id), power, name, description, hidden)));

		return direct.mapResult(PowerParsingEvents.RESULT_MAPPER);

	}

	public static MapCodec<PowerHolder<?>> mapCodec(MapCodec<Power> powerCodec) {

		MapCodec<PowerHolder<?>> direct = RecordCodecBuilder.mapCodec(instance -> instance.group(
			PowerIdentifier.CODEC.fieldOf(ID_KEY).forGetter(PowerHolder::id),
			powerCodec.forGetter(PowerHolder::value),
			ComponentSerialization.CODEC.optionalFieldOf("name", Component.empty()).forGetter(PowerHolder::name),
			ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(PowerHolder::description),
			BooleanProvider.CODEC.optionalFieldOf("hidden", new ConstantBooleanProvider(false)).forGetter(PowerHolder::hidden)
		).apply(instance, PowerHolder::new));

		return direct.mapResult(PowerParsingEvents.RESULT_MAPPER);

	}

}
