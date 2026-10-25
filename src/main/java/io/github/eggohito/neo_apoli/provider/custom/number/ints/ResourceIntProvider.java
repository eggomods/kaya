package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.custom.IntResourcePower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ResourceIntProvider(PowerIdentifier power, EntityProvider entity) implements IntProvider {

	public static final MapCodec<ResourceIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(ResourceIntProvider::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(ResourceIntProvider::entity)
	).apply(instance, ResourceIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ResourceIntProvider> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, ResourceIntProvider::power,
		EntityProvider.STREAM_CODEC, ResourceIntProvider::entity,
		ResourceIntProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliIntProviderTypes.RESOURCE;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.flatMap(Powers::getOptional)
			.flatMap(powers -> powers.getOptionalInstance(power()))
			.filter(IntResourcePower.Instance.class::isInstance)
			.map(IntResourcePower.Instance.class::cast)
			.map(IntResourcePower.Instance::getValue);
	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		power().validate(validator.forChild(".power"), IntResourcePower.class, () -> power().asDisplayString() + " is not a resource of an int type!");
		entity().validate(validator.forChild(".entity"));
	}

}
