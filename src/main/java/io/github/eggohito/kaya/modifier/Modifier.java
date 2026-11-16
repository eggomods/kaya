package io.github.eggohito.kaya.modifier;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.modifier.custom.AddModifier;
import io.github.eggohito.kaya.modifier.custom.MultiplyAdditiveModifier;
import io.github.eggohito.kaya.modifier.custom.MultiplyMultiplicativeModifier;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.CodecUtil;
import io.github.eggohito.kaya.util.MapCodecUtil;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.StreamCodecUtil;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.DoubleStream;

public interface Modifier extends ContextUser, Comparable<Modifier> {

	Codec<Modifier> CODEC = Type.CODEC.dispatch(Modifier::getType, Type::mapCodec);

	StreamCodec<RegistryFriendlyByteBuf, Modifier> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Modifier::getType, Type::streamCodec);

	@Override
	default void validate(ContextValidator validator) {
		ContextUser.super.validate(validator);
		ContextValidatable.validate(modifiers(), validator, index -> ".modifiers[" + index + "]");
	}

	@Override
	default int compareTo(@NotNull Modifier that) {

		if (this.phase() == that.phase()) {
			return Orderer.INSTANCE.compare(this, that);
		}

		else {
			return this.phase().compareTo(that.phase());
		}

	}

	Type<?> getType();

	List<Modifier> modifiers();

	FloatProvider amount();

	Phase phase();

	double apply(DoubleStream amounts, double base, double total);

	default Operation asOperation(Context context) {
		return new Operation(this, context);
	}

	static <M extends Modifier> Products.P3<RecordCodecBuilder.Mu<M>, List<Modifier>, FloatProvider, Phase> addFields(RecordCodecBuilder.Instance<M> instance) {
		return instance.group(
			MapCodecUtil.lazy(() -> Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of())).forGetter(Modifier::modifiers),
			FloatProvider.CODEC.fieldOf("amount").forGetter(Modifier::amount),
			Phase.CODEC.fieldOf("phase").forGetter(Modifier::phase)
		);
	}

	static <M extends Modifier> MapCodec<M> mapCodec(Function3<List<Modifier>, FloatProvider, Phase, M> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> addFields(instance).apply(instance, constructor));
	}

	static <M extends Modifier> StreamCodec<RegistryFriendlyByteBuf, M> streamCodec(Function3<List<Modifier>, FloatProvider, Phase, M> constructor) {
		return StreamCodec.composite(
			StreamCodecUtil.lazy(() -> Modifier.STREAM_CODEC.apply(ByteBufCodecs.list())), Modifier::modifiers,
			FloatProvider.STREAM_CODEC, Modifier::amount,
			Phase.STREAM_CODEC, Modifier::phase,
			constructor
		);
	}

	static Modifier fromVanilla(AttributeModifier vanillaModifier) {

		AttributeModifier.Operation operation = vanillaModifier.operation();
		float amount = (float) vanillaModifier.amount();

		return switch (operation) {
			case ADD_VALUE ->
				new AddModifier(List.of(), new ConstantFloatProvider(amount), Modifier.Phase.BASE);
			case ADD_MULTIPLIED_BASE ->
				new MultiplyAdditiveModifier(List.of(), new ConstantFloatProvider(amount), Modifier.Phase.BASE);
			case ADD_MULTIPLIED_TOTAL ->
				new MultiplyMultiplicativeModifier(List.of(), new ConstantFloatProvider(amount), Modifier.Phase.TOTAL);
		};

	}

	static double applyAll(List<Operation> operations, double baseValue) {

		if (operations.isEmpty()) {
			return baseValue;
		}

		Set<Operation> sorted = new ObjectAVLTreeSet<>(Operation::compareTo);
		DoubleList amounts = new DoubleArrayList();

		sorted.addAll(operations);
		Iterator<Operation> sortedIterator = sorted.iterator();

		//  Collect all the modifiers' amounts
		while (sortedIterator.hasNext()) {

			Operation operation = sortedIterator.next();

			Context context = operation.context();
			Modifier modifier = operation.modifier();

			List<Operation> nestedOperations = new ObjectArrayList<>();
			MiscUtil.iterateList(modifier.modifiers(), (index, nested) -> nestedOperations.add(nested.asOperation(context.forChild(".modifiers[" + index + "]"))));

			Context amountContext = context.forChild(".amount");
			double amount = modifier.amount().getFloat(amountContext);

			if (amountContext.hasProblems()) {
				sortedIterator.remove();
			}

			else {
				amounts.add(applyAll(nestedOperations, amount));
			}

		}

		Phase previousPhase = null;
		double base = baseValue, total = base;

		//  Apply the modifiers' calculations
		for (var operation : sorted) {

			Modifier modifier = operation.modifier();
			Phase currentPhase = modifier.phase();

			if (currentPhase != previousPhase) {
				previousPhase = currentPhase;
				base = total;
			}

			total = modifier.apply(amounts.doubleStream(), base, total);

		}

		return total;

	}

	static Operation operation(Modifier modifier, Context context) {
		return new Operation(modifier, context);
	}

	record Operation(Modifier modifier, Context context) implements Comparable<Operation> {

		@Override
		public int compareTo(@NotNull Modifier.Operation that) {
			return this.modifier().compareTo(that.modifier());
		}

	}

	enum Phase {

		BASE,
		TOTAL;

		public static final Codec<Phase> CODEC = CodecUtil.enumType(Phase.class);
		public static final StreamCodec<ByteBuf, Phase> STREAM_CODEC = StreamCodecUtil.enumType(Phase.class);

	}

	record Type<M extends Modifier>(MapCodec<M> mapCodec, StreamCodec<RegistryFriendlyByteBuf, M> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.MODIFIER_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

		@Override
		public @NotNull String toString() {
			return Util.getRegisteredName(KayaRegistries.MODIFIER_TYPE, this);
		}

	}

	final class Orderer implements AutoCloseable {

		public static final Orderer INSTANCE = new Orderer();

		private final List<Modifier.Type<?>> order = new ObjectArrayList<>();
		private boolean frozen = false;

		private Orderer() {

		}

		@Override
		public void close() {

			List<Modifier.Type<?>> unordered = new ObjectArrayList<>();

			for (var registered : KayaRegistries.MODIFIER_TYPE) {

				if (!order.contains(registered)) {
					unordered.add(registered);
				}

			}

			if (!unordered.isEmpty()) {
				throw new IllegalStateException("The following modifier types weren't added to the orderer: " + unordered);
			}

			this.frozen = true;

		}

		public Orderer addBefore(Modifier.Type<?> target, Modifier.Type<?> type) {

			validateChange();
			this.findIndex(target, index -> {

				order.remove(type);

				if (index <= 0) {
					order.addFirst(type);
				}

				else {
					order.add(index - 1, type);
				}

			});

			return this;

		}

		public Orderer addAfter(Modifier.Type<?> target, Modifier.Type<?> type) {

			validateChange();
			this.findIndex(target, index -> {

				order.remove(type);

				if (index >= order.size()) {
					order.addLast(type);
				}

				else {
					order.add(index + 1, type);
				}

			});

			return this;

		}

		public Orderer addFirst(Modifier.Type<?> type) {

			validateChange();

			order.remove(type);
			order.addFirst(type);

			return this;

		}

		public Orderer addLast(Modifier.Type<?> type) {

			validateChange();

			order.remove(type);
			order.addLast(type);

			return this;

		}

		public int compare(Modifier first, Modifier second) {

			int firstIndex = this.getIndex(first.getType());
			int secondIndex = this.getIndex(second.getType());

			return Integer.compare(firstIndex, secondIndex);

		}

		private int getIndex(Modifier.Type<?> type) {

			MutableInt index = new MutableInt(0);
			this.findIndex(type, index::setValue);

			return index.intValue();

		}

		private void findIndex(Modifier.Type<?> target, IntConsumer visitor) {

			for (int i = 0; i < order.size(); i++) {

				var type = order.get(i);

				if (type == target) {
					visitor.accept(i);
					break;
				}

			}

		}

		private void validateChange() {

			if (this.frozen) {
				throw new IllegalStateException("Modifier orderer is already frozen!");
			}

		}

	}

}
