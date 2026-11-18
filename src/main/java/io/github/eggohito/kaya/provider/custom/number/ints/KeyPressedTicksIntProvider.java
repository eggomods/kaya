package io.github.eggohito.kaya.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.key.KeyState;
import io.github.eggohito.kaya.key.manager.KeyStateManager;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.provider.custom.string.StringProvider;
import io.github.eggohito.kaya.registry.provider.KayaIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record KeyPressedTicksIntProvider(StringProvider id, EntityProvider entity) implements IntProvider {

	public static final MapCodec<KeyPressedTicksIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		StringProvider.CODEC.fieldOf("id").forGetter(KeyPressedTicksIntProvider::id),
		EntityProvider.CODEC.fieldOf("entity").forGetter(KeyPressedTicksIntProvider::entity)
	).apply(instance, KeyPressedTicksIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, KeyPressedTicksIntProvider> STREAM_CODEC = StreamCodec.composite(
		StringProvider.STREAM_CODEC, KeyPressedTicksIntProvider::id,
		EntityProvider.STREAM_CODEC, KeyPressedTicksIntProvider::entity,
		KeyPressedTicksIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return KayaIntProviderTypes.KEY_PRESSED_TICKS;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return id().getValue(context.forChild(".id"))
			.flatMap(id -> entity().getValue(context.forChild(".entity"))
				.flatMap(entity -> KeyStateManager.getInstance().getCurrentState(entity.getUUID(), id)
					.filter(KeyState::pressed)
					.map(state -> (int) (context.level().getGameTime() - state.pressedTime()))));
	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		id().validate(validator.forChild(".id"));
		entity().validate(validator.forChild(".entity"));
	}

}
