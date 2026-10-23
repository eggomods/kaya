package io.github.eggohito.neo_apoli.util.key;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

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

	public record TimingWindow(IntProvider min, IntProvider max) implements ContextValidatable {

		public static final TimingWindow DEFAULT = new TimingWindow(3, 3);

		public static final Codec<TimingWindow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IntProvider.CODEC.fieldOf("min").forGetter(TimingWindow::min),
			IntProvider.CODEC.fieldOf("max").forGetter(TimingWindow::max)
		).apply(instance, TimingWindow::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, TimingWindow> STREAM_CODEC = StreamCodec.composite(
			IntProvider.STREAM_CODEC, TimingWindow::min,
			IntProvider.STREAM_CODEC, TimingWindow::max,
			TimingWindow::new
		);

		public TimingWindow(int min, int max) {
			this(new ConstantIntProvider(min), new ConstantIntProvider(max));
		}

		@Override
		public void validate(ContextValidator validator) {
			min().validate(validator.forChild(".min"));
			max().validate(validator.forChild(".max"));
		}

	}

}
