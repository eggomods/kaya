package io.github.eggohito.neo_apoli.registry;

import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.comparison.Comparison;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.container_menu.ContainerMenu;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.hud.element.HudElement;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.box.BoxProvider;
import io.github.eggohito.neo_apoli.provider.custom.command_source.CommandSourceProvider;
import io.github.eggohito.neo_apoli.provider.custom.damage_source.DamageSourceProvider;
import io.github.eggohito.neo_apoli.provider.custom.direction.DirectionProvider;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.item.ItemProvider;
import io.github.eggohito.neo_apoli.provider.custom.nbt.NbtProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.slot.SlotProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class NeoApoliRegistries {

	public static final Registry<Power.Type<?>> POWER_TYPE = create(NeoApoliRegistryKeys.POWER_TYPE);
	public static final Registry<Action.Type<?>> ACTION_TYPE = create(NeoApoliRegistryKeys.ACTION_TYPE);
	public static final Registry<Condition.Type<?>> CONDITION_TYPE = create(NeoApoliRegistryKeys.CONDITION_TYPE);

	public static final Registry<BlockProvider.Type<?>> BLOCK_PROVIDER_TYPE = create(NeoApoliRegistryKeys.BLOCK_PROVIDER_TYPE);
	public static final Registry<BooleanProvider.Type<?>> BOOLEAN_PROVIDER_TYPE = create(NeoApoliRegistryKeys.BOOLEAN_PROVIDER_TYPE);
	public static final Registry<BoxProvider.Type<?>> BOX_PROVIDER_TYPE = create(NeoApoliRegistryKeys.BOX_PROVIDER_TYPE);
	public static final Registry<CommandSourceProvider.Type<?>> COMMAND_SOURCE_PROVIDER_TYPE = create(NeoApoliRegistryKeys.COMMAND_SOURCE_PROVIDER_TYPE);
	public static final Registry<DamageSourceProvider.Type<?>> DAMAGE_SOURCE_PROVIDER_TYPE = create(NeoApoliRegistryKeys.DAMAGE_SOURCE_PROVIDER_TYPE);
	public static final Registry<DirectionProvider.Type<?>> DIRECTION_PROVIDER_TYPE = create(NeoApoliRegistryKeys.DIRECTION_PROVIDER_TYPE);
	public static final Registry<EffectProvider.Type<?>> EFFECT_PROVIDER_TYPE = create(NeoApoliRegistryKeys.EFFECT_PROVIDER_TYPE);
	public static final Registry<EntityProvider.Type<?>> ENTITY_PROVIDER_TYPE = create(NeoApoliRegistryKeys.ENTITY_PROVIDER_TYPE);
	public static final Registry<FloatProvider.Type<?>> FLOAT_PROVIDER_TYPE = create(NeoApoliRegistryKeys.FLOAT_PROVIDER_TYPE);
	public static final Registry<IntProvider.Type<?>> INT_PROVIDER_TYPE = create(NeoApoliRegistryKeys.INT_PROVIDER_TYPE);
	public static final Registry<ItemProvider.Type<?>> ITEM_PROVIDER_TYPE = create(NeoApoliRegistryKeys.ITEM_PROVIDER_TYPE);
	public static final Registry<NbtProvider.Type<?>> NBT_PROVIDER_TYPE = create(NeoApoliRegistryKeys.NBT_PROVIDER_TYPE);
	public static final Registry<SlotProvider.Type<?>> SLOT_PROVIDER_TYPE = create(NeoApoliRegistryKeys.SLOT_PROVIDER_TYPE);
	public static final Registry<StringProvider.Type<?>> STRING_PROVIDER_TYPE = create(NeoApoliRegistryKeys.STRING_PROVIDER_TYPE);
	public static final Registry<Vec3Provider.Type<?>> VEC3_PROVIDER_TYPE = create(NeoApoliRegistryKeys.VEC3_PROVIDER_TYPE);

	public static final Registry<Comparison.Type<?>> COMPARISON_TYPE = create(NeoApoliRegistryKeys.COMPARISON_TYPE);
	public static final Registry<ContainerMenu.Type<?>> CONTAINER_MENU_TYPE = create(NeoApoliRegistryKeys.CONTAINER_MENU_TYPE);
	public static final Registry<Modifier.Type<?>> MODIFIER_TYPE = create(NeoApoliRegistryKeys.MODIFIER_TYPE);
	public static final Registry<Color.Type<?>> COLOR_TYPE = create(NeoApoliRegistryKeys.COLOR_TYPE);
	public static final Registry<HudElement.Type<?>> HUD_ELEMENT_TYPE = create(NeoApoliRegistryKeys.HUD_ELEMENT_TYPE);

	public static final Registry<ContextParameter.Type<?>> CONTEXT_PARAMETER_TYPE = create(NeoApoliRegistryKeys.CONTEXT_PARAMETER_TYPE);
	public static final Registry<ValueProvider.Kind<?>> VALUE_PROVIDER_KIND = create(NeoApoliRegistryKeys.VALUE_PROVIDER_KIND);

	private static <T> Registry<T> create(ResourceKey<Registry<T>> key) {
		return FabricRegistryBuilder.createSimple(key)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();
	}

}
