package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.visitor.ClearableVisitor;
import io.github.eggohito.kaya.event.PowerModifyEvents;
import io.github.eggohito.kaya.modifier.Modifier;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.entity.Powers;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.CachedBlock;
import io.github.eggohito.kaya.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ModifyBlockBreakSpeedPower(List<Modifier> modifiers, Properties properties) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();

	public static final MapCodec<ModifyBlockBreakSpeedPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf()).fieldOf("modifiers").forGetter(ModifyBlockBreakSpeedPower::modifiers),
		Power.propertiesCodecBuilder(true)
	).apply(instance, ModifyBlockBreakSpeedPower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyBlockBreakSpeedPower> STREAM_CODEC = StreamCodec.composite(
		Modifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ModifyBlockBreakSpeedPower::modifiers,
		Properties.STREAM_CODEC, ModifyBlockBreakSpeedPower::properties,
		ModifyBlockBreakSpeedPower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.MODIFY_BLOCK_BREAK_SPEED;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends Power.Instance<ModifyBlockBreakSpeedPower> {

		protected Instance(@NotNull ModifyBlockBreakSpeedPower power) {
			super(power);
		}

		public Context createContext(Entity holder, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
			return this.createHolderContextBuilder(holder)
					.withRequired(KayaContextParameters.MINING_BLOCK, new CachedBlock(blockPos, blockState, blockEntity))
				.build(holder.level());
		}

		public List<Modifier.Operation> modifiers(Context context) {

			List<Modifier.Operation> result = new ObjectArrayList<>();
			MiscUtil.iterateList(power.modifiers(), (index, modifier) -> result.add(Modifier.operation(modifier, context.forChild(".modifiers[" + index + "]"))));

			return result;

		}

	}

	public static float modify(Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, float breakSpeed) {

		List<Modifier.Operation> entries = new ObjectArrayList<>();

		for (var instance : Powers.getInstances(player, Instance.class)) {

			try {

				Context context = instance.createContext(player, blockPos, blockState, blockEntity);

				if (VISITOR.push(instance) && instance.isActive(context)) {
					entries.addAll(instance.modifiers(context));
				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		PowerModifyEvents.NUMBER.invoker().beforeModified(KayaPowerTypes.MODIFY_JUMP, entries, breakSpeed);
		return (float) Modifier.applyAll(entries, breakSpeed);

	}

}
