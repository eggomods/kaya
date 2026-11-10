package io.github.eggohito.kaya.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.action.Action;
import io.github.eggohito.kaya.codec.KayaCodecs;
import io.github.eggohito.kaya.codec.KayaStreamCodecs;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.context.parameter.BlockContextParameter;
import io.github.eggohito.kaya.context.parameter.DirectionContextParameter;
import io.github.eggohito.kaya.mixin.access.UseOnContextAccessor;
import io.github.eggohito.kaya.power.Power;
import io.github.eggohito.kaya.power.custom.misc.PrioritizedPower;
import io.github.eggohito.kaya.registry.KayaPowerTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public record CallbackBlockPlacePower(Action onPlaceAction, EnumSet<Direction> directions, EnumSet<InteractionHand> hands, int priority, Properties properties) implements PrioritizedPower<CallbackBlockBreakPower> {
	
	public static final BlockContextParameter PLACED_ON_BLOCK = new BlockContextParameter(Kaya.id("placed_on_block"));
	public static final BlockContextParameter PLACED_TO_BLOCK = new BlockContextParameter(Kaya.id("placed_to_block"));
	public static final DirectionContextParameter PLACED_SIDE = new DirectionContextParameter(Kaya.id("placed_side"));

	public static final MapCodec<CallbackBlockPlacePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Action.CODEC.fieldOf("on_place_action").forGetter(CallbackBlockPlacePower::onPlaceAction),
		KayaCodecs.DIRECTION_SET.optionalFieldOf("directions", EnumSet.allOf(Direction.class)).forGetter(CallbackBlockPlacePower::directions),
		KayaCodecs.HAND_SET.optionalFieldOf("hands", EnumSet.allOf(InteractionHand.class)).forGetter(CallbackBlockPlacePower::hands),
		Codec.INT.optionalFieldOf("priority", 0).forGetter(CallbackBlockPlacePower::priority),
		Power.propertiesCodecBuilder(true)
	).apply(instance, CallbackBlockPlacePower::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CallbackBlockPlacePower> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC, CallbackBlockPlacePower::onPlaceAction,
		KayaStreamCodecs.DIRECTION_SET, CallbackBlockPlacePower::directions,
		KayaStreamCodecs.HAND_SET, CallbackBlockPlacePower::hands,
		ByteBufCodecs.INT, CallbackBlockPlacePower::priority,
		Properties.STREAM_CODEC, CallbackBlockPlacePower::properties,
		CallbackBlockPlacePower::new
	);

	@Override
	public Type<?> getType() {
		return KayaPowerTypes.CALLBACK_BLOCK_PLACE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(ContextValidator validator) {
		PrioritizedPower.super.validate(validator);
		onPlaceAction().validate(validator.forChild(".on_place_action"));
	}

	public static final class Instance extends Power.Instance<CallbackBlockPlacePower> {

		Instance(@NotNull CallbackBlockPlacePower power) {
			super(power);
		}
		
		public Context createContext(Player holder, Level level, BlockPos onPos, BlockPos toPos, Direction onSide, InteractionHand hand) {
			return this.createHolderContextBuilder(holder)
				.withOptional(PLACED_ON_BLOCK, CachedBlock.optionallyFromLoadedPos(level, onPos))
				.withOptional(PLACED_TO_BLOCK, CachedBlock.optionallyFromLoadedPos(level, toPos))
				.withRequired(PLACED_SIDE, onSide)
				.withRequired(KayaContextParameters.USED_ITEM, holder.getItemInHand(hand))
				.withRequired(KayaContextParameters.USED_ITEM_SLOT, SlotAccess.of(() -> holder.getItemInHand(hand), stack -> holder.setItemInHand(hand, stack)))
				.build(level);
		}
		
		public boolean doesApply(Direction side, InteractionHand hand) {
			return power.directions().contains(side)
				&& power.hands().contains(hand);
		}
		
		public void executeActions(Context context) {
			power.onPlaceAction().execute(context.forChild(".on_place_action"));
		}

	}
	
	public static void execute(BlockPlaceContext placeContext) {

		Direction placedSide = ((UseOnContextAccessor) placeContext).getHitResult().getDirection();
		BlockPos placedOnPos = ((UseOnContextAccessor) placeContext).getHitResult().getBlockPos();
		BlockPos placedToPos = placeContext.getClickedPos();
		
		InteractionHand hand = placeContext.getHand();
		Player player = placeContext.getPlayer();
		
		for (var instance : new InstanceCollection<>(player, Instance.class, instance -> instance.doesApply(placedSide, hand))) {
			
			Context context = instance.createContext(player, placeContext.getLevel(), placedOnPos, placedToPos, placedSide, hand);
			
			if (instance.isActive(context)) {
				instance.executeActions(context);
			}
			
		}
		
	}

}
