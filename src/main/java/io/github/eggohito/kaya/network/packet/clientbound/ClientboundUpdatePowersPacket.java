package io.github.eggohito.kaya.network.packet.clientbound;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.power.PowerHolder;
import io.github.eggohito.kaya.power.PowerIdentifier;
import io.github.eggohito.kaya.power.manager.PowerManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ClientboundUpdatePowersPacket(Map<PowerIdentifier, PowerHolder<?>> powers, Map<ResourceLocation, List<PowerHolder<?>>> tags) implements CustomPacketPayload {

	public static final Type<ClientboundUpdatePowersPacket> TYPE = new Type<>(PowerManager.ID.withPath(path -> "clientbound/" + path + "/update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdatePowersPacket> CODEC = StreamCodec.ofMember(ClientboundUpdatePowersPacket::send, ClientboundUpdatePowersPacket::receive);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	private void send(RegistryFriendlyByteBuf buf) {

		Map<PowerIdentifier, PowerHolder<?>> filtered = new Object2ObjectLinkedOpenHashMap<>(powers());
		filtered.keySet().removeIf(PowerIdentifier::isSubPower);

		buf.writeInt(filtered.size());

		for (var powerEntry : filtered.entrySet()) {

			PowerIdentifier id = powerEntry.getKey();
			PowerIdentifier.STREAM_CODEC.encode(buf, id);

			try {
				PowerHolder.STREAM_CODEC.encode(buf, powerEntry.getValue());
			}

			catch (Exception e) {
				Kaya.LOGGER.error("Couldn't encode {} during the syncing process", id.asDisplayString(false), e);
				throw e;
			}

		}

		buf.writeInt(tags().size());

		for (var tagEntry : tags().entrySet()) {

			ResourceLocation id = tagEntry.getKey();
			ResourceLocation.STREAM_CODEC.encode(buf, id);

			List<PowerHolder<?>> holders = tagEntry.getValue();
			buf.writeInt(holders.size());

			for (var holder : holders) {
				PowerIdentifier.STREAM_CODEC.encode(buf, holder.id());
			}

		}

	}

	private static ClientboundUpdatePowersPacket receive(RegistryFriendlyByteBuf buf) {

		Map<PowerIdentifier, PowerHolder<?>> powers = new Object2ObjectLinkedOpenHashMap<>();
		int powersCount = buf.readInt();

		for (int i = 0; i < powersCount; i++) {

			PowerIdentifier id = PowerIdentifier.STREAM_CODEC.decode(buf);

			try {
				powers.put(id, PowerHolder.STREAM_CODEC.decode(buf));
			}

			catch (Exception e) {
				Kaya.LOGGER.error("Couldn't decode {} during the syncing process", id.asDisplayString(false), e);
				throw e;
			}

		}

		Map<ResourceLocation, List<PowerHolder<?>>> tags = new Object2ObjectLinkedOpenHashMap<>();
		int tagsCount = buf.readInt();

		for (int i = 0; i < tagsCount; i++) {

			ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
			int count = buf.readInt();

			for (int j = 0; j < count; j++) {

				try {

					PowerIdentifier holderId = PowerIdentifier.STREAM_CODEC.decode(buf);
					PowerHolder<?> holder = Objects.requireNonNull(powers.get(holderId), "Unknown " + holderId.asDisplayString(false));

					tags
						.computeIfAbsent(id, k -> new ObjectArrayList<>())
						.add(holder);

				}

				catch (Exception e) {
					Kaya.LOGGER.error("Couldn't decode power tag \"{}\" during the syncing process", id, e);
					throw e;
				}

			}

		}

		return new ClientboundUpdatePowersPacket(powers, tags);

	}

}
