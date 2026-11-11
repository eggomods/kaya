package io.github.eggohito.kaya.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.entity.EntityProvider;
import io.github.eggohito.kaya.provider.custom.number.IntProvider;
import io.github.eggohito.kaya.registry.provider.KayaIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PlayerXPNextIntProvider(EntityProvider entity) implements IntProvider {

	public static final MapCodec<PlayerXPNextIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityProvider.CODEC.fieldOf("entity").forGetter(PlayerXPNextIntProvider::entity))
		.apply(instance, PlayerXPNextIntProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerXPNextIntProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, PlayerXPNextIntProvider::entity,
		PlayerXPNextIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return KayaIntProviderTypes.PLAYER_XP_NEXT;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.filter(Player.class::isInstance)
			.map(Player.class::cast)
			.map(Player::getXpNeededForNextLevel);
	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}
