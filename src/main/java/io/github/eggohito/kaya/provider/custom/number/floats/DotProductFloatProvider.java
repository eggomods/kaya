package io.github.eggohito.kaya.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidatable;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.kaya.registry.provider.KayaFloatProviderTypes;
import io.github.eggohito.kaya.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record DotProductFloatProvider(List<Vec3Provider> vectors) implements FloatProvider {

	public static final MapCodec<DotProductFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(ExtraCodecs.nonEmptyList(Vec3Provider.CODEC.listOf()).fieldOf("vectors").forGetter(DotProductFloatProvider::vectors))
		.apply(instance, DotProductFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, DotProductFloatProvider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC.apply(ByteBufCodecs.list()), DotProductFloatProvider::vectors,
		DotProductFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return KayaFloatProviderTypes.DOT_PRODUCT;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		MutableFloat result = new MutableFloat();
		MutableBoolean init = new MutableBoolean(false);

		MutableBoolean accumulated = new MutableBoolean(false);
		Mutable<Vec3> identity = new MutableObject<>();

		MiscUtil.iterateList(
			vectors(),
			(index, provider) -> {

				Context vectorContext = context.forChild(".vectors[" + index + "]");
				Vec3 vector = provider.getValue(vectorContext).orElse(null);

				if (vector != null) {

					if (init.isTrue()) {
						result.setValue(identity.getValue().dot(vector));
						accumulated.setTrue();
					}

					identity.setValue(vector);
					init.setTrue();

				}

			}
		);

		return accumulated.isTrue()
			? Optional.of(result.floatValue())
			: Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		ContextValidatable.validate(vectors(), validator, index -> ".vectors[" + index + "]");
	}

}
