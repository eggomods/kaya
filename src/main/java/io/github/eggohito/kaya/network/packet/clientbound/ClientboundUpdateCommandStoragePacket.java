package io.github.eggohito.kaya.network.packet.clientbound;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.duck.internal.CommandStorageHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundUpdateCommandStoragePacket(ResourceLocation id, CompoundTag data) implements CustomPacketPayload {

	public static final Type<ClientboundUpdateCommandStoragePacket> TYPE = new Type<>(Kaya.id("clientbound/update_command_storage"));
	public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateCommandStoragePacket> CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, ClientboundUpdateCommandStoragePacket::id,
		ByteBufCodecs.TRUSTED_COMPOUND_TAG, ClientboundUpdateCommandStoragePacket::data,
		ClientboundUpdateCommandStoragePacket::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(CommandStorageHolder storageHolder) {
		storageHolder.kaya$setStorage(this.id(), this.data());
	}

}
