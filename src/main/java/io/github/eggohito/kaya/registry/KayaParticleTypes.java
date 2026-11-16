package io.github.eggohito.kaya.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.util.alias.FixedRegistryAlias;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class KayaParticleTypes {

	public static final FixedRegistryAlias<ParticleType<?>> ALIASES = FixedRegistryAlias.of(BuiltInRegistries.PARTICLE_TYPE, Kaya.GLOBAL_ALIASES);

	public static final Codec<ParticleType<?>> CODEC = ALIASES.createCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, ParticleType<?>> STREAM_CODEC = ALIASES.createRegistryStreamCodec();

	public static final Codec<ParticleOptions> OPTIONS_CODEC = CODEC.dispatch(ParticleOptions::getType, ParticleType::codec);
	public static final StreamCodec<RegistryFriendlyByteBuf, ParticleOptions> OPTIONS_STREAM_CODEC = STREAM_CODEC.dispatch(ParticleOptions::getType, ParticleType::streamCodec);

	public static final SimpleParticleType NOTHING = registerInternal("nothing", false);

	public static void registerAll() {

	}

	private static SimpleParticleType registerInternal(String name, boolean alwaysShow) {
		return register(Kaya.id(name), alwaysShow);
	}

	public static SimpleParticleType register(ResourceLocation id, boolean alwaysShow) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, FabricParticleTypes.simple(alwaysShow));
	}

	private static <P extends ParticleOptions> ParticleType<P> registerInternal(String name, boolean alwaysShow, Function<ParticleType<P>, MapCodec<P>> codecGetter, Function<ParticleType<P>, StreamCodec<? super RegistryFriendlyByteBuf, P>> streamCodecGetter) {
		return register(Kaya.id(name), alwaysShow, codecGetter, streamCodecGetter);
	}

	public static <P extends ParticleOptions> ParticleType<P> register(ResourceLocation id, boolean alwaysShow, Function<ParticleType<P>, MapCodec<P>> codecGetter, Function<ParticleType<P>, StreamCodec<? super RegistryFriendlyByteBuf, P>> streamCodecGetter) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, new ParticleType<P>(alwaysShow) {

			@Override
			public MapCodec<P> codec() {
				return codecGetter.apply(this);
			}

			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, P> streamCodec() {
				return streamCodecGetter.apply(this);
			}

		});
	}

}
