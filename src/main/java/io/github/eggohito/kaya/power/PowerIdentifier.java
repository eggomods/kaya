package io.github.eggohito.kaya.power;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.power.custom.MultiplePower;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.util.MiscUtil;
import io.github.eggohito.kaya.util.ResourceLocationUtil;
import io.github.eggohito.kaya.util.StringDisplayable;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public sealed interface PowerIdentifier extends StringDisplayable, ContextValidatable {

	Codec<PowerIdentifier> CODEC = PrimitiveCodec.STRING.comapFlatMap(PowerIdentifier::parseAsResult, PowerIdentifier::toString);

	StreamCodec<ByteBuf, PowerIdentifier> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(PowerIdentifier::parse, PowerIdentifier::toString);

	char SEPARATOR = '@';

	ResourceLocation baseId();

	String createTranslationKey();

	boolean isSubPower();

	default void validate(ContextValidator validator, Class<? extends Power> powerClass, Supplier<String> errorSupplier) {
		PowerManager.getInstance().getAsResult(this)
			.map(PowerHolder::value)
			.flatMap(MiscUtil.validateType(powerClass, errorSupplier))
			.resultOrPartial(validator::reportProblem);
	}

	@Override
	default void validate(ContextValidator validator) {
		PowerManager.getInstance().getAsResult(this).resultOrPartial(validator::reportProblem);
	}

	static PowerIdentifier of(@NotNull ResourceLocation id) {
		return new Impl(id);
	}

	static PowerIdentifier subPowerOf(@NotNull ResourceLocation id, @NotNull String name) {
		return new SubImpl(id, name);
	}

	static PowerIdentifier parse(String input) {
		int separatorIndex = input.indexOf(SEPARATOR);
		return separatorIndex >= 0
			? subPowerOf(ResourceLocation.parse(input.substring(0, separatorIndex)), input.substring(separatorIndex + 1))
			: of(ResourceLocation.parse(input));
	}

	static DataResult<PowerIdentifier> parseAsResult(String input) {

		try {
			return DataResult.success(parse(input));
		}

		catch (Exception e) {
			return DataResult.error(e::getMessage);
		}

	}

	static PowerIdentifier read(StringReader reader) throws CommandSyntaxException {

		int cursor = reader.getCursor();
		String input = readGreedily(reader);

		try {
			return parse(input);
		}

		catch (ResourceLocationException e) {
			reader.setCursor(cursor);
			throw MiscUtil.createCommandExceptionWithContext(reader, e::getMessage);
		}

	}

	static boolean isCharAllowed(char ch) {
		return ch == SEPARATOR
			|| ResourceLocation.isAllowedInResourceLocation(ch)
			|| ResourceLocationUtil.isEnabledAndPlaceholder(ch);
	}

	private static String readGreedily(StringReader reader) {

		int cursor = reader.getCursor();

		while (reader.canRead() && isCharAllowed(reader.peek())) {
			reader.skip();
		}

		return reader.getString().substring(cursor, reader.getCursor());

	}

	record Impl(ResourceLocation baseId) implements PowerIdentifier {


		public Impl {
			ResourceLocationUtil.validateNonEmpty(baseId).getOrThrow();
		}

		@Override
		public String createTranslationKey() {
			return Util.makeDescriptionId("power", this.baseId());
		}

		@Override
		public boolean isSubPower() {
			return false;
		}

		@Override
		public String asDisplayString() {
			return "Power \"" + this.baseId() + "\"";
		}

		@Override
		public @NotNull String toString() {
			return this.baseId().toString();
		}

	}

	record SubImpl(ResourceLocation baseId, String name) implements PowerIdentifier {

		public SubImpl {
			ResourceLocationUtil.validateNonEmpty(baseId).getOrThrow();
			MultiplePower.validateSubPowerName(name).getOrThrow();
		}

		@Override
		public String createTranslationKey() {
			return Util.makeDescriptionId("power", this.baseId()) + SEPARATOR + this.name();
		}

		@Override
		public boolean isSubPower() {
			return true;
		}

		@Override
		public String asDisplayString() {
			return "Sub-power \"" + this.name() + "\" of power \"" + this.baseId() + "\"";
		}

		@Override
		public @NotNull String toString() {
			return this.baseId() + Character.toString(SEPARATOR) + this.name();
		}

	}

}
