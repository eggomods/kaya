package io.github.eggohito.kaya.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.github.eggohito.kaya.command.argument.PowerArgument;
import io.github.eggohito.kaya.mixin.access.TagEntryAccessor;
import io.github.eggohito.kaya.util.*;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class KayaStreamCodecs {

	public static final StreamCodec<ByteBuf, Set<ResourceLocation>> IDENTIFIER_SET = ByteBufCodecs.collection(ObjectOpenHashSet::new, ResourceLocation.STREAM_CODEC);

	public static final StreamCodec<ByteBuf, Set<ResourceLocation>> NON_EMPTY_IDENTIFIER_SET = StreamCodecUtil.nonEmptyCollection(IDENTIFIER_SET);

	public static final StreamCodec<ByteBuf, InteractionHand> HAND = StreamCodecUtil.enumType(InteractionHand.class);

	public static final StreamCodec<ByteBuf, List<InteractionHand>> HANDS = ByteBufCodecs.collection(ObjectArrayList::new, HAND);

	public static final StreamCodec<ByteBuf, EnumSet<InteractionHand>> HAND_SET = HANDS.map(EnumSet::copyOf, ObjectArrayList::new);

	public static final StreamCodec<ByteBuf, NbtPathArgument.NbtPath> NBT_PATH = ByteBufCodecs.fromCodecTrusted(NbtPathArgument.NbtPath.CODEC);

	public static final StreamCodec<ByteBuf, LightLayer> LIGHT_TYPE = StreamCodecUtil.enumType(LightLayer.class);

	public static final StreamCodec<ByteBuf, List<Direction>> DIRECTIONS = ByteBufCodecs.collection(ObjectArrayList::new, Direction.STREAM_CODEC);

	public static final StreamCodec<ByteBuf, EnumSet<Direction>> DIRECTION_SET = DIRECTIONS.map(EnumSet::copyOf, ObjectArrayList::new);

	public static final StreamCodec<ByteBuf, Direction.Axis> AXIS = StreamCodecUtil.enumType(Direction.Axis.class);

	public static final StreamCodec<ByteBuf, Explosion.BlockInteraction> BLOCK_INTERACTION = StreamCodecUtil.enumType(Explosion.BlockInteraction.class);

	public static final StreamCodec<FriendlyByteBuf, InteractionResult> INTERACTION_RESULT = StreamCodecUtil.mapped(MiscUtil.INTERACTION_RESULTS);

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockInput> BLOCK_INPUT = ByteBufCodecs.fromCodecWithRegistriesTrusted(KayaCodecs.STRINGIFIED_BLOCK_INPUT);

	public static final StreamCodec<ByteBuf, TagKey<EntityType<?>>> ENTITY_TYPE_TAG = TagKey.streamCodec(Registries.ENTITY_TYPE);

	public static final StreamCodec<ByteBuf, Set<TagKey<EntityType<?>>>> ENTITY_TYPE_TAG_SET = ByteBufCodecs.collection(ObjectOpenHashSet::new, ENTITY_TYPE_TAG);

	public static final StreamCodec<RegistryFriendlyByteBuf, List<AttributedModifier>> ATTRIBUTE_MODIFIERS = ByteBufCodecs.collection(ObjectArrayList::new, AttributedModifier.STREAM_CODEC);

	public static final StreamCodec<ByteBuf, Dynamic<?>> PASSTHROUGH = ByteBufCodecs.fromCodec(Codec.PASSTHROUGH);

	public static final StreamCodec<RegistryFriendlyByteBuf, Dynamic<?>> REGISTRY_PASSTHROUGH = ByteBufCodecs.fromCodecWithRegistriesTrusted(Codec.PASSTHROUGH);

	public static final StreamCodec<RegistryFriendlyByteBuf, CraftingRecipe> CRAFTING_RECIPE = Recipe.STREAM_CODEC.map(
		recipe -> RecipeUtil.validateCraftingRecipe(recipe).getOrThrow(),
		Function.identity()
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<CraftingRecipe>> CRAFTING_RECIPE_ENTRY = RecipeHolder.STREAM_CODEC.map(
		recipeEntry -> new RecipeHolder<>(recipeEntry.id(), RecipeUtil.validateCraftingRecipe(recipeEntry.value()).getOrThrow()),
		Function.identity()
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ClipContext.Block> BLOCK_CLIP_CONTEXT = StreamCodecUtil.enumType(ClipContext.Block.class);

	public static final StreamCodec<RegistryFriendlyByteBuf, ClipContext.Fluid> FLUID_CLIP_CONTEXT = StreamCodecUtil.enumType(ClipContext.Fluid.class);

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityAnchorArgument.Anchor> ENTITY_ANCHOR = StreamCodecUtil.enumType(EntityAnchorArgument.Anchor.class);

	public static final StreamCodec<RegistryFriendlyByteBuf, Difficulty> DIFFICULTY = StreamCodecUtil.enumType(Difficulty.class);

	public static final StreamCodec<ByteBuf, ExtraCodecs.TagOrElementLocation> TAG_OR_ELEMENT_ID = ByteBufCodecs.STRING_UTF8.map(
		str -> str.startsWith("#")
			? ResourceLocation.read(str.substring(1)).map(id -> new ExtraCodecs.TagOrElementLocation(id, true)).getOrThrow()
			: ResourceLocation.read(str).map(id -> new ExtraCodecs.TagOrElementLocation(id, false)).getOrThrow(),
		ExtraCodecs.TagOrElementLocation::toString
	);

	public static final StreamCodec<ByteBuf, TagEntry> TAG_ENTRY = StreamCodec.composite(
		TAG_OR_ELEMENT_ID, tagEntry -> ((TagEntryAccessor) tagEntry).callElementOrTag(),
		ByteBufCodecs.BOOL, tagEntry -> ((TagEntryAccessor) tagEntry).isRequired(),
		TagEntryAccessor::createTagEntry
	);

	public static final StreamCodec<ByteBuf, List<TagEntry>> TAG_ENTRIES = ByteBufCodecs.collection(ObjectArrayList::new, TAG_ENTRY);

	public static final StreamCodec<ByteBuf, RoundingMode> ROUNDING_MODE = StreamCodecUtil.enumType(RoundingMode.class);

	public static final StreamCodec<ByteBuf, Pattern> PATTERN = ByteBufCodecs.STRING_UTF8.map(Pattern::compile, Pattern::pattern);

	public static final StreamCodec<ByteBuf, Map<Pattern, String>> REPLACEMENT_MAP = ByteBufCodecs.map(Object2ObjectOpenHashMap::new, PATTERN, ByteBufCodecs.STRING_UTF8);

	public static final StreamCodec<ByteBuf, ParsedArgument<EntitySelector>> ENTITY_SELECTOR = ParsedArgument.streamCodec(EntityArgument.entity());

	public static final StreamCodec<ByteBuf, Biome.Precipitation> PRECIPITATION = StreamCodecUtil.enumType(Biome.Precipitation.class);

	public static final StreamCodec<ByteBuf, SlotRange> SLOT_RANGE = ByteBufCodecs.STRING_UTF8.map(name -> Objects.requireNonNull(SlotRanges.nameToIds(name), "Unknown slot range: \"" + name + "\""), StringRepresentable::getSerializedName);

	public static final StreamCodec<RegistryFriendlyByteBuf, ParsedArgument<ItemPredicateArgument.Result>> ITEM_PREDICATE = ParsedArgument.streamCodecWithSimpleContext(ItemPredicateArgument::itemPredicate);

	public static final StreamCodec<ByteBuf, SoundSource> SOUND_SOURCE = StreamCodecUtil.enumType(SoundSource.class);

	public static final StreamCodec<ByteBuf, ParsedArgument<PowerArgument.Result>> POWER_OR_TAG_ARGUMENT = ParsedArgument.streamCodec(PowerArgument.powerOrTag());

	public static final StreamCodec<ByteBuf, ResourceKey<Biome>> BIOME_KEY = ResourceKey.streamCodec(Registries.BIOME);

	public static final StreamCodec<ByteBuf, TagKey<Biome>> BIOME_TAG = TagKey.streamCodec(Registries.BIOME);

	public static final StreamCodec<ByteBuf, Either<ResourceKey<Biome>, TagKey<Biome>>> BIOME_KEY_OR_TAG = ByteBufCodecs.either(BIOME_KEY, BIOME_TAG);

	public static final StreamCodec<ByteBuf, ResourceKey<Structure>> STRUCTURE_KEY = ResourceKey.streamCodec(Registries.STRUCTURE);

	public static final StreamCodec<ByteBuf, TagKey<Structure>> STRUCTURE_TAG = TagKey.streamCodec(Registries.STRUCTURE);

	public static final StreamCodec<ByteBuf, Either<ResourceKey<Structure>, TagKey<Structure>>> STRUCTURE_KEY_OR_TAG = ByteBufCodecs.either(STRUCTURE_KEY, STRUCTURE_TAG);

	public static final StreamCodec<ByteBuf, Character> CHARACTER = ByteBufCodecs.STRING_UTF8.map(MiscUtil::unsafelyAssumeStringAsCharacter, String::valueOf);

}
