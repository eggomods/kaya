package io.github.eggohito.kaya.hud.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextUser;
import io.github.eggohito.kaya.registry.KayaRegistries;
import io.github.eggohito.kaya.util.CodecUtil;
import io.github.eggohito.kaya.util.StreamCodecUtil;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface HudElement extends ContextUser {

	Codec<HudElement> CODEC = Type.CODEC.dispatch(HudElement::getType, Type::mapCodec);

	StreamCodec<RegistryFriendlyByteBuf, HudElement> STREAM_CODEC = Type.STREAM_CODEC.dispatch(HudElement::getType, Type::streamCodec);

	Type<?> getType();

	int order();

	default boolean shouldRender(Context context, RenderPhase renderPhase) {
		return renderPhase == RenderPhase.ABOVE_HUD;
	}

	default boolean hideWithHud(Context context) {
		return true;
	}

	@Getter
	@Setter
	class Position {
		int x;
		int y;
	}

	record WithContext(Context context, HudElement element) implements Comparable<WithContext> {

		@Override
		public int compareTo(@NotNull WithContext that) {
			return Integer.compare(this.element().order(), that.element().order());
		}

	}

	record Type<E extends HudElement>(MapCodec<E> mapCodec, StreamCodec<RegistryFriendlyByteBuf, E> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(KayaRegistries.HUD_ELEMENT_TYPE, Kaya.GLOBAL_ALIASES);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(Kaya.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	}

	enum RenderPhase {

		BELOW_HUD,
		ABOVE_HUD;

		public static final Codec<RenderPhase> CODEC = CodecUtil.enumType(RenderPhase.class);
		public static final StreamCodec<ByteBuf, RenderPhase> STREAM_CODEC = StreamCodecUtil.enumType(RenderPhase.class);

	}

}
