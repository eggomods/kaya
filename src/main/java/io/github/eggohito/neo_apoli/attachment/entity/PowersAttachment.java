package io.github.eggohito.neo_apoli.attachment.entity;

import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.mixin.access.EntityAccessor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.PowerHolder;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.power.manager.PowerManager;
import io.github.eggohito.neo_apoli.registry.attachment.NeoApoliEntityAttachments;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public record PowersAttachment(ImmutableMap<PowerIdentifier, Power.Instance<?>> instances, ImmutableSetMultimap<PowerIdentifier, ResourceLocation> sources, Optional<String> decodingErrors) {

	public static final PowersAttachment EMPTY = new PowersAttachment();

	public static final Codec<PowersAttachment> CODEC = new Codec<>() {

		@Override
		public <T> DataResult<Pair<PowersAttachment, T>> decode(DynamicOps<T> ops, T input) {
			return Entry.LIST_CODEC.parse(ops, input).flatMap(entries -> unpack(ops, entries)).map(attachment -> Pair.of(attachment, input));
		}

		@Override
		public <T> DataResult<T> encode(PowersAttachment input, DynamicOps<T> ops, T prefix) {
			return input.pack(ops).flatMap(packed -> Entry.LIST_CODEC.encode(packed, ops, prefix));
		}

	};

	public static final StreamCodec<RegistryFriendlyByteBuf, PowersAttachment> STREAM_CODEC = new StreamCodec<>() {

		private static final StreamCodec<RegistryFriendlyByteBuf, Optional<List<Entry>>> OPTIONAL_ENTRIES_CODEC = ByteBufCodecs.optional(Entry.LIST_STREAM_CODEC);

		@Override
		public @NotNull PowersAttachment decode(RegistryFriendlyByteBuf buf) {
			RegistryOps<Tag> ops = buf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
			return OPTIONAL_ENTRIES_CODEC.decode(buf).flatMap(self -> unpack(ops, self).resultOrPartial()).orElse(EMPTY);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, PowersAttachment input) {
			RegistryOps<Tag> ops = buf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
			OPTIONAL_ENTRIES_CODEC.encode(buf, input.pack(ops).resultOrPartial());
		}

	};

	public PowersAttachment(ImmutableMap<PowerIdentifier, Power.Instance<?>> instances, ImmutableSetMultimap<PowerIdentifier, ResourceLocation> sources) {
		this(instances, sources, Optional.empty());
	}

	private PowersAttachment() {
		this(ImmutableMap.of(), ImmutableSetMultimap.of(), Optional.empty());
	}

	@Override
	public @NotNull String toString() {
		return "PowersAttachment[%s]".formatted(this == EMPTY ? "EMPTY" : "instances=%s, sources=%s, decodingErrors=%s".formatted(instances(), sources(), decodingErrors()));
	}

	private <T> DataResult<List<Entry>> pack(DynamicOps<T> ops) {

		Set<Entry> entries = new ObjectLinkedOpenHashSet<>();
		DataResult<Unit> identity = DataResult.success(Unit.INSTANCE);

		for (var instanceEntry : instances.entrySet()) {

			PowerIdentifier id = instanceEntry.getKey();
			Power.Instance<?> instance = instanceEntry.getValue();

			Set<ResourceLocation> sources = this.sources.get(id);
			DataResult<T> encodedData = instance.encodeData(ops);

			T data = encodedData.mapOrElse(Function.identity(), error -> ops.emptyMap());
			identity = identity.apply2stable((unit, t) -> unit, encodedData);

			Entry packed = new Entry(id, instance.power().getType(), sources, new Dynamic<>(ops, data));
			entries.add(packed);

		}

		ImmutableList<Entry> entriesAsList = ImmutableList.copyOf(entries);
		return identity.map(unit -> entriesAsList)
			.setPartial(entriesAsList)
			.map(Function.identity());

	}

	private void invokeCallback(PowerIdentifier id, Consumer<Power.Instance<?>> callback) {

		Power.Instance<?> instance = this.instances().get(id);

		if (instance != null) {
			callback.accept(instance);
		}

	}

	public static void onChanged(@NotNull Entity holder, @Nullable PowersAttachment oldValue, @Nullable PowersAttachment newValue) {

		//  If there is no new value and an old value, it means the attachment was removed
		if (newValue == null && oldValue != null) {

			for (var instance : oldValue.instances().values()) {
				instance.onRevoked(holder);
				instance.onRemoved(holder);
			}

		}

		//  If there is no old value and a new value, it means the attachment was initialized
		else if (oldValue == null && newValue != null) {

			//  Skip invoking the granted/added callbacks if it's the entity's first tick
			//  (which is the case if the entity is newly spawned)
			if (((EntityAccessor) holder).isFirstTick()) {
				return;
			}

			for (var instance : newValue.instances().values()) {
				instance.onGranted(holder);
				instance.onAdded(holder);
			}

		}

		//  If both old and new values are present, it means the attachment was changed
		else if (oldValue != null) {

			//  Get the difference between the old and new attachments (for initial callbacks)
			Set<PowerIdentifier> revoked = Sets.difference(oldValue.instances().keySet(), newValue.instances().keySet());
			Set<PowerIdentifier> granted = Sets.difference(newValue.instances().keySet(), oldValue.instances().keySet());

			//  Iterate through all the powers that has been revoked/granted
			revoked.forEach(id -> oldValue.invokeCallback(id, instance -> instance.onRevoked(holder)));
			granted.forEach(id -> newValue.invokeCallback(id, instance -> instance.onGranted(holder)));

			//  Get the difference between the sources of the old and new attachments (for recurring callbacks)
			Set<Map.Entry<PowerIdentifier, ResourceLocation>> removed = Sets.difference(oldValue.sources().entries(), newValue.sources().entries());
			Set<Map.Entry<PowerIdentifier, ResourceLocation>> added = Sets.difference(newValue.sources().entries(), oldValue.sources().entries());

			//  Iterate through all the powers that has been removed/added
			removed.forEach(entry -> oldValue.invokeCallback(entry.getKey(), instance -> instance.onRemoved(holder)));
			added.forEach(entry -> newValue.invokeCallback(entry.getKey(), instance -> instance.onAdded(holder)));

		}

	}

	//  TODO:   Pass the identity as the result since decoding/encoding data attachments in FAPI in version
	//          26.1.x now promotes its partial result
	/**
	 *  <p>Enforce partial result as success result since FAPI doesn't allow partial results when serializing data
	 *  attachments (yet?)</p>
	 *
	 *  <p>Also cache the errors encountered during decoding to be logged at a later point with the context of the
	 *  attachment target (which in this case, is an {@link net.minecraft.world.entity.Entity})</p>
	 */
	private static <T> DataResult<PowersAttachment> unpack(DynamicOps<T> ops, List<Entry> entries) {

		Object2ObjectMap<PowerIdentifier, Power.Instance<?>> instancesMap = new Object2ObjectLinkedOpenHashMap<>();
		SetMultimap<PowerIdentifier, ResourceLocation> sourcesMap = LinkedHashMultimap.create();

		DataResult<Unit> identity = DataResult.success(Unit.INSTANCE);

		for (var entry : entries) {

			PowerIdentifier powerId = entry.id();
			DataResult<PowerHolder<?>> powerResult = PowerManager.getInstance().getAsResult(powerId);

			identity = identity.apply2stable((unit, power) -> unit, powerResult);

			if (powerResult.isError()) {
				continue;
			}

			Power power = powerResult.getOrThrow().value();
			Power.Instance<?> instance = power.createInstance();

			Dynamic<T> data = entry.data().convert(ops);
			Set<ResourceLocation> sources = entry.sources();

			if (Objects.equals(entry.type(), power.getType())) {
				identity = identity.apply2stable((identityUnit, decodeUnit) -> identityUnit, instance.decodeData(ops, data.getValue()));
			}

			else {
				identity = identity.apply2stable((unit, o) -> unit, DataResult.error(() -> "Couldn't transfer old data of " + entry.id().asDisplayString(false) + ", as it's now using a different power type!"));
			}

			instancesMap.put(powerId, instance);
			sourcesMap.putAll(powerId, sources);

		}

		PowersAttachment result = new PowersAttachment(ImmutableMap.copyOf(instancesMap), ImmutableSetMultimap.copyOf(sourcesMap), identity.error().map(DataResult.Error::message));
		return DataResult.success(result);

	}

	//  TODO:   Remove this once the codebase has been ported to 26.1.x since decoding/encoding data attachments
	//          in FAPI in that version promotes its partial result
	private static void onLoad(Entity entity, ServerLevel serverLevel) {

		PowersAttachment attachment = entity.getAttached(NeoApoliEntityAttachments.POWERS);

		if (attachment != null) {
			attachment.decodingErrors().ifPresent(errors -> NeoApoli.logOnce(Level.WARN, "Found error(s) while decoding powers attachment on entity %s: %s".formatted(entity.getName().getString(), errors)));
		}

	}

	record Entry(PowerIdentifier id, Power.Type<?> type, Set<ResourceLocation> sources, Dynamic<?> data) {

		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PowerIdentifier.CODEC.fieldOf("id").forGetter(Entry::id),
			Power.Type.CODEC.fieldOf("type").forGetter(Entry::type),
			NeoApoliCodecs.NON_EMPTY_IDENTIFIER_SET.fieldOf("sources").forGetter(Entry::sources),
			Codec.PASSTHROUGH.fieldOf("data").forGetter(Entry::data)
		).apply(instance, Entry::new));

		public static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();

		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
			PowerIdentifier.STREAM_CODEC, Entry::id,
			Power.Type.STREAM_CODEC, Entry::type,
			NeoApoliStreamCodecs.NON_EMPTY_IDENTIFIER_SET, Entry::sources,
			NeoApoliStreamCodecs.REGISTRY_PASSTHROUGH, Entry::data,
			Entry::new
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, List<Entry>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

	}

	static {
		ServerEntityEvents.ENTITY_LOAD.register(Powers.ID, PowersAttachment::onLoad);
	}

}
