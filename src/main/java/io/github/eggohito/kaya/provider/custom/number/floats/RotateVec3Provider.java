package io.github.eggohito.kaya.provider.custom.number.floats;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.kaya.context.Context;
import io.github.eggohito.kaya.context.ContextValidator;
import io.github.eggohito.kaya.provider.custom.number.FloatProvider;
import io.github.eggohito.kaya.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.kaya.registry.provider.KayaVec3ProviderTypes;
import io.github.eggohito.kaya.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record RotateVec3Provider(Vec3Provider vector, Optional<FloatProvider> pitch, Optional<FloatProvider> yaw, Optional<FloatProvider> roll) implements Vec3Provider {

	private static final MapCodec<RotateVec3Provider> UNVALIDATED_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("vector").forGetter(RotateVec3Provider::vector),
		FloatProvider.CODEC.optionalFieldOf("pitch").forGetter(RotateVec3Provider::pitch),
		FloatProvider.CODEC.optionalFieldOf("yaw").forGetter(RotateVec3Provider::yaw),
		FloatProvider.CODEC.optionalFieldOf("roll").forGetter(RotateVec3Provider::roll)
	).apply(instance, RotateVec3Provider::new));

	public static final MapCodec<RotateVec3Provider> CODEC = UNVALIDATED_CODEC.mapResult(MiscUtil.validateWithInput(
		(provider, input) -> provider.validateKeys().mapError(message -> " in map: " + input)
	));

	public static final StreamCodec<RegistryFriendlyByteBuf, RotateVec3Provider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, RotateVec3Provider::vector,
		ByteBufCodecs.optional(FloatProvider.STREAM_CODEC), RotateVec3Provider::pitch,
		ByteBufCodecs.optional(FloatProvider.STREAM_CODEC), RotateVec3Provider::yaw,
		ByteBufCodecs.optional(FloatProvider.STREAM_CODEC), RotateVec3Provider::roll,
		RotateVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return KayaVec3ProviderTypes.ROTATE;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return vector().getValue(context.forChild(".vector")).map(vector -> this.rotate(context, vector));
	}

	@Override
	public void validate(ContextValidator validator) {
		Vec3Provider.super.validate(validator);
		vector().validate(validator.forChild(".vector"));
		validateKeys()
			.ifError(error -> validator.reportProblem(error.message()))
			.ifSuccess(ignored -> {
				pitch().ifPresent(pitch -> pitch.validate(validator.forChild(".pitch")));
				yaw().ifPresent(yaw -> yaw.validate(validator.forChild(".yaw")));
				roll().ifPresent(roll -> roll.validate(validator.forChild(".roll")));
			});
	}

	private DataResult<RotateVec3Provider> validateKeys() {

		List<String> missingKeys = new ObjectArrayList<>();

		if (pitch().isEmpty()) {
			missingKeys.add("pitch");
		}

		if (yaw().isEmpty()) {
			missingKeys.add("yaw");
		}

		if (roll().isEmpty()) {
			missingKeys.add("roll");
		}

		if (missingKeys.size() >= 3) {
			return DataResult.error(() -> "Any of " + MiscUtil.anyKeys(missingKeys) + " keys must be present");
		}

		else {
			return DataResult.success(this);
		}

	}

	private Vec3 rotate(Context context, Vec3 vector) {

		vector = pitch()
			.flatMap(pitch -> pitch.getValue(context.forChild(".pitch")))
			.map(vector::xRot)
			.orElse(vector);
		vector = yaw()
			.flatMap(yaw -> yaw.getValue(context.forChild(".pitch")))
			.map(vector::yRot)
			.orElse(vector);
		vector = roll()
			.flatMap(roll -> roll.getValue(context.forChild(".pitch")))
			.map(vector::zRot)
			.orElse(vector);

		return vector;

	}

}
