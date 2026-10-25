package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.ItemContextParameter;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.PrioritizedPower;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ReplaceLootTablePower(Optional<Condition> activeCondition, Map<Pattern, String> replacements, int priority) implements PrioritizedPower<ReplaceLootTablePower> {

	public static final ResourceKey<LootTable> REPLACED_TABLE_KEY = ResourceKey.create(Registries.LOOT_TABLE, NeoApoli.id("replaced_loot_table"));

	public static final BlockContextParameter BLOCK = new BlockContextParameter(NeoApoli.id("block"));
	public static final ItemContextParameter TOOL_ITEM = new ItemContextParameter(NeoApoli.id("tool_item"));

	public static final MapCodec<ReplaceLootTablePower> CODEC = RecordCodecBuilder.mapCodec(instance -> Power
		.addActiveConditionField(instance)
		.and(NeoApoliCodecs.REPLACEMENT_MAP.fieldOf("replacements").forGetter(ReplaceLootTablePower::replacements))
		.and(Codec.INT.optionalFieldOf("priority", 0).forGetter(ReplaceLootTablePower::priority))
		.apply(instance, ReplaceLootTablePower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ReplaceLootTablePower> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Condition.STREAM_CODEC), Power::activeCondition,
		NeoApoliStreamCodecs.REPLACEMENT_MAP, ReplaceLootTablePower::replacements,
		ByteBufCodecs.INT, ReplaceLootTablePower::priority,
		ReplaceLootTablePower::new
	);

	private static final Stack<LootTable> REPLACEMENT_STACK = new Stack<>();
	private static final Stack<LootTable> BACKTRACK_STACK = new Stack<>();

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.REPLACE_LOOT_TABLE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<ReplaceLootTablePower> {

		protected Instance(@NotNull ReplaceLootTablePower power) {
			super(power);
		}

		public Context createContext(Entity holder, LootContext lootContext) {

			ServerLevel serverLevel = lootContext.getLevel();
			Optional<BlockPos> blockPos = Optional.ofNullable(lootContext.getOptionalParameter(LootContextParams.ORIGIN))
				.map(BlockPos::containing);
			Optional<BlockState> blockState = Optional.ofNullable(lootContext.getOptionalParameter(LootContextParams.BLOCK_STATE))
				.or(() -> blockPos.map(serverLevel::getBlockState));
			Optional<BlockEntity> blockEntity = Optional.ofNullable(lootContext.getOptionalParameter(LootContextParams.BLOCK_ENTITY))
				.or(() -> blockPos.flatMap(pos -> Optional.ofNullable(serverLevel.getBlockEntity(pos))));

			return this.createHolderContextBuilder(holder)
				.withRequired(NeoApoliContextParameters.ACTOR_ENTITY, holder)
				.withNullable(NeoApoliContextParameters.TARGET_ENTITY, lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY))
				.withOptional(BLOCK, blockPos.flatMap(pos -> blockState.map(state -> new CachedBlock(pos, state, blockEntity.orElse(null)))))
				.withNullable(TOOL_ITEM, lootContext.getOptionalParameter(LootContextParams.TOOL))
				.build(lootContext.getLevel());

		}

		public Optional<ResourceKey<LootTable>> getReplacement(Context context, ResourceKey<LootTable> key) {

			String tableId = key.location().toString();
			ResourceLocationUtil.setCurrent(this.id.value());

			for (Map.Entry<Pattern, String> entry : power.replacements().entrySet()) {

				Pattern regex = entry.getKey();
				String replacement = entry.getValue();

				Matcher matcher = regex.matcher(tableId);
				if (matcher.matches()) {

					try {

						String replaced = matcher.replaceAll(replacement);
						ResourceKey<LootTable> replacedKey = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(replaced));

						return Optional.of(replacedKey);

					}

					catch (ResourceLocationException e) {
						context.forChild(".\"" + regex.pattern() + "\"").reportProblem(e.getMessage());
					}

				}

			}

			ResourceLocationUtil.setCurrent(null);
			return Optional.empty();

		}

	}

	public static LootTable push(LootTable lootTable) {
		return REPLACEMENT_STACK.push(lootTable);
	}

	public static LootTable pop() {

		if (REPLACEMENT_STACK.isEmpty()) {
			return LootTable.EMPTY;
		}

		LootTable popped = REPLACEMENT_STACK.pop();
		BACKTRACK_STACK.push(popped);

		return popped;

	}

	public static LootTable restore() {

		if (BACKTRACK_STACK.isEmpty()) {
			return LootTable.EMPTY;
		}

		LootTable restored = BACKTRACK_STACK.pop();
		REPLACEMENT_STACK.push(restored);

		return restored;

	}

	public static LootTable peek() {

		if (REPLACEMENT_STACK.isEmpty()) {
			return LootTable.EMPTY;
		}

		else {
			return REPLACEMENT_STACK.peek();
		}

	}

	public static void clear() {
		REPLACEMENT_STACK.clear();
		BACKTRACK_STACK.clear();
	}

}
