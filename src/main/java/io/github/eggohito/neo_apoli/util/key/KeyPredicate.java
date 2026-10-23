package io.github.eggohito.neo_apoli.util.key;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.key.manager.KeyStateManager;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public record KeyPredicate(StringProvider id, TimingWindow timingWindow) implements ContextValidatable {

	public static final Codec<KeyPredicate> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		StringProvider.CODEC.fieldOf("id").forGetter(KeyPredicate::id),
		TimingWindow.CODEC.optionalFieldOf("timing_window", TimingWindow.DEFAULT).forGetter(KeyPredicate::timingWindow)
	).apply(instance, KeyPredicate::new));

	public static final Codec<KeyPredicate> INLINE_CODEC = StringProvider.CODEC.xmap(
		KeyPredicate::new,
		KeyPredicate::id
	);

	public static final Codec<KeyPredicate> CODEC = Codec.withAlternative(DIRECT_CODEC, INLINE_CODEC);

	public static final StreamCodec<RegistryFriendlyByteBuf, KeyPredicate> STREAM_CODEC = StreamCodec.composite(
		StringProvider.STREAM_CODEC, KeyPredicate::id,
		TimingWindow.STREAM_CODEC, KeyPredicate::timingWindow,
		KeyPredicate::new
	);

	public KeyPredicate(StringProvider id) {
		this(id, TimingWindow.DEFAULT);
	}

	@Override
	public void validate(ContextValidator validator) {
		id().validate(validator.forChild(".id"));
		timingWindow().validate(validator.forChild(".timing_window"));
	}

	public Optional<State> getState(UUID uuid, Context context) {

		KeyState state = id()
			.getValue(context.forChild(".id"))
			.flatMap(id -> KeyStateManager.getInstance().getCurrentState(uuid, id))
			.orElse(null);

		if (state == null) {
			return Optional.empty();
		}

		else {
			return Optional.of(new State(state, timingWindow().get(context.forChild(".timing_window"))));
		}

	}

	public record TimingWindow(IntProvider lowerOffset, IntProvider upperOffset) implements ContextValidatable {

		public static final TimingWindow DEFAULT = new TimingWindow(3, 3);

		public static final Codec<TimingWindow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IntProvider.CODEC.fieldOf("lower_offset").forGetter(TimingWindow::lowerOffset),
			IntProvider.CODEC.fieldOf("upper_offset").forGetter(TimingWindow::upperOffset)
		).apply(instance, TimingWindow::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, TimingWindow> STREAM_CODEC = StreamCodec.composite(
			IntProvider.STREAM_CODEC, TimingWindow::lowerOffset,
			IntProvider.STREAM_CODEC, TimingWindow::upperOffset,
			TimingWindow::new
		);

		public TimingWindow(int min, int max) {
			this(new ConstantIntProvider(min), new ConstantIntProvider(max));
		}

		@Override
		public void validate(ContextValidator validator) {
			lowerOffset().validate(validator.forChild(".lower_offset"));
			upperOffset().validate(validator.forChild(".upper_offset"));
		}

		public IntIntPair get(Context context) {
			return IntIntPair.of(
				lowerOffset().getInt(context.forChild(".lower_offset")),
				upperOffset().getInt(context.forChild(".upper_offset"))
			);
		}

	}

	public record State(KeyState key, int lowerOffset, int upperOffset) {

		public State(KeyState state, IntIntPair offset) {
			this(state, offset.leftInt(), offset.rightInt());
		}

	}

}
