package io.github.eggohito.kaya.codec;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.mixin.access.BlockInputAccessor;
import io.github.eggohito.kaya.mixin.access.TagParserAccessor;
import io.github.eggohito.kaya.registry.KayaParticleTypes;
import io.github.eggohito.kaya.util.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.TriState;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;

import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public class KayaCodecs {

	public static final Codec<ResourceLocation> MOD_NAMESPACE_IDENTIFIER = ResourceLocationUtil.codecWithDefaultNamespace(Kaya.MOD_NAMESPACE);

	public static final Codec<Set<ResourceLocation>> IDENTIFIER_SET = ResourceLocation.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

	public static final Codec<Set<ResourceLocation>> NON_EMPTY_IDENTIFIER_SET = CodecUtil.nonEmptySet(IDENTIFIER_SET);

	public static final Codec<InteractionHand> HAND = CodecUtil.enumType(InteractionHand.class, MiscUtil.HANDS);

	public static final Codec<List<InteractionHand>> HANDS = ExtraCodecs.nonEmptyList(HAND.listOf());

	public static final Codec<EnumSet<InteractionHand>> HAND_SET = HANDS.xmap(EnumSet::copyOf, ObjectArrayList::new);

	public static final Codec<LightLayer> LIGHT_TYPE = CodecUtil.enumType(LightLayer.class);

	public static final Codec<Explosion.BlockInteraction> BLOCK_INTERACTION = CodecUtil.enumType(Explosion.BlockInteraction.class);

	public static final Codec<Direction> DIRECTION = CodecUtil.enumType(Direction.class);

	public static final Codec<List<Direction>> DIRECTIONS = DIRECTION.listOf();

	public static final Codec<EnumSet<Direction>> DIRECTION_SET = DIRECTIONS.xmap(EnumSet::copyOf, ObjectArrayList::new);

	public static final Codec<InteractionResult> INTERACTION_RESULT = CodecUtil.mapped(MiscUtil.INTERACTION_RESULTS);

	public static final Codec<Tag> STRINGIFIED_TAG = Codec.STRING.comapFlatMap(
		str -> {

			try {
				return DataResult.success(TagParserAccessor.getDefaultReader().parseFully(str));
			}

			catch (CommandSyntaxException e) {
				return DataResult.error(() -> "Error parsing string NBT: " + e.getMessage());
			}

		},
		Tag::toString
	);

	public static final Codec<Tag> TAG = Codec.PASSTHROUGH.xmap(
		dynamic -> dynamic.convert(NbtOps.INSTANCE).getValue(),
		nbtElement -> new Dynamic<>(NbtOps.INSTANCE, nbtElement)
	);

	public static final Codec<CompoundTag> STRINGIFIED_COMPOUND_TAG = Codec.STRING.comapFlatMap(
		str -> {

			try {
				return DataResult.success(TagParser.parseCompoundFully(str));
			}

			catch (CommandSyntaxException e) {
				return DataResult.error(() -> "Error parsing string NBT: " + e.getMessage());
			}

		},
		CompoundTag::toString
	);

	public static final Codec<CompoundTag> COMPOUND_TAG = TAG.comapFlatMap(MiscUtil::asCompoundTag, Function.identity());

	public static final Codec<Tag> REGULAR_OR_STRINGIFIED_TAG = new MultiAlternativeCodec<>(TAG, STRINGIFIED_TAG);

	public static final Codec<CompoundTag> REGULAR_OR_STRINGIFIED_COMPOUND_TAG = new MultiAlternativeCodec<>(COMPOUND_TAG, STRINGIFIED_COMPOUND_TAG);

	public static final Codec<BlockInput> BLOCK_INPUT = KayaMapCodecs.BLOCK_INPUT.codec();

	public static final Codec<BlockInput> STRINGIFIED_BLOCK_INPUT = Codec.STRING.comapFlatMap(
		input -> {

			try {

				BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, input, true);
				BlockInput blockInput = new BlockInput(blockResult.blockState(), blockResult.properties().keySet(), blockResult.nbt());

				return DataResult.success(blockInput);

			}

			catch (CommandSyntaxException e) {
				return DataResult.error(() -> "Couldn't parse string as block input: " + e.getMessage());
			}

		},
		blockInput -> {

			StringBuilder result = new StringBuilder(BlockStateParser.serialize(blockInput.getState()));
			CompoundTag tag = ((BlockInputAccessor) blockInput).getTag();

			if (tag != null) {
				result.append(tag);
			}

			return result.toString();

		}
	);

	public static final Codec<BlockInput> REGULAR_OR_STRINGIFIED_BLOCK_INPUT = new MultiAlternativeCodec<>(BLOCK_INPUT, STRINGIFIED_BLOCK_INPUT);

	public static final Codec<List<AttributedModifier>> NONEMPTY_ATTRIBUTE_MODIFIERS = ExtraCodecs.nonEmptyList(AttributedModifier.CODEC.listOf());

	public static final Codec<Vec3> VECTOR_3_DOUBLE = new MultiAlternativeCodec<>(Vec3.CODEC, KayaMapCodecs.VECTOR_3_DOUBLE.codec());

	public static final Codec<ClipContext.Block> BLOCK_CLIP_CONTEXT = CodecUtil.enumType(ClipContext.Block.class);

	public static final Codec<ClipContext.Fluid> FLUID_CLIP_CONTEXT = CodecUtil.enumType(ClipContext.Fluid.class);

	public static final Codec<EntityAnchorArgument.Anchor> ENTITY_ANCHOR = CodecUtil.enumType(EntityAnchorArgument.Anchor.class);

	public static final Codec<Difficulty> DIFFICULTY = CodecUtil.enumType(Difficulty.class);

	public static final Codec<List<TagEntry>> COMPACT_TAG_ENTRIES = ExtraCodecs.compactListCodec(TagEntry.CODEC);

	public static final Codec<SimpleParticleType> SIMPLE_PARTICLE = KayaParticleTypes.CODEC.comapFlatMap(
		particleType -> particleType instanceof SimpleParticleType simpleParticleType
			? DataResult.success(simpleParticleType)
			: DataResult.error(() -> "Particle \"" + RegistryUtil.getId(BuiltInRegistries.PARTICLE_TYPE, particleType) + "\" requires parameters!"),
		Function.identity()
	);

	public static final Codec<ParticleOptions> PARTICLE_OPTIONS = Codec.withAlternative(KayaParticleTypes.OPTIONS_CODEC, SIMPLE_PARTICLE);

	public static final Codec<RoundingMode> ROUNDING_MODE = CodecUtil.enumType(RoundingMode.class);

	public static final Codec<Map<Pattern, String>> REPLACEMENT_MAP = ExtraCodecs.strictUnboundedMap(ExtraCodecs.PATTERN, Codec.STRING);

	public static final Codec<ParsedArgument<EntitySelector>> ENTITY_SELECTOR = ParsedArgument.codec(EntityArgument.entity());

	public static final Codec<Biome.Precipitation> PRECIPITATION = CodecUtil.enumType(Biome.Precipitation.class);

	public static final Codec<ParsedArgument<ItemPredicateArgument.Result>> ITEM_PREDICATE = ParsedArgument.codecWithSimpleContext(ItemPredicateArgument::itemPredicate);

	public static final Codec<SoundSource> SOUND_SOURCE = CodecUtil.enumType(SoundSource.class);

	public static final Codec<ResourceKey<Biome>> BIOME_KEY = ResourceKey.codec(Registries.BIOME);

	public static final Codec<TagKey<Biome>> BIOME_TAG = TagKey.hashedCodec(Registries.BIOME);

	public static final Codec<Either<ResourceKey<Biome>, TagKey<Biome>>> BIOME_KEY_OR_TAG = Codec.either(BIOME_KEY, BIOME_TAG);

	public static final Codec<ResourceKey<Structure>> STRUCTURE_KEY = ResourceKey.codec(Registries.STRUCTURE);

	public static final Codec<TagKey<Structure>> STRUCTURE_TAG = TagKey.hashedCodec(Registries.STRUCTURE);

	public static final Codec<Either<ResourceKey<Structure>, TagKey<Structure>>> STRUCTURE_KEY_OR_TAG = Codec.either(STRUCTURE_KEY, STRUCTURE_TAG);

	public static final Codec<Character> CHARACTER = Codec.STRING.comapFlatMap(MiscUtil::validateStringAsCharacter, String::valueOf);

	public static final Codec<TriState> TRI_STATE = CodecUtil.enumType(TriState.class);

	public static final Codec<JsonElement> JSON_ELEMENT = Codec.PASSTHROUGH.xmap(
		dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
		jsonElement -> new Dynamic<>(JsonOps.INSTANCE, jsonElement)
	);

	public static final Codec<GameEventListener.DeliveryMode> DELIVERY_MODE = CodecUtil.enumType(GameEventListener.DeliveryMode.class);

}
