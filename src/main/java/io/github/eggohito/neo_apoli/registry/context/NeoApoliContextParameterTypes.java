package io.github.eggohito.neo_apoli.registry.context;

import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.parameter.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class NeoApoliContextParameterTypes {

	public static final ContextParameter.Type<BlockContextParameter> BLOCK = registerInternal("block", BlockContextParameter::new);
	public static final ContextParameter.Type<BooleanContextParameter> BOOLEAN = registerInternal("boolean", BooleanContextParameter::new);
	public static final ContextParameter.Type<BoxContextParameter> BOX = registerInternal("box", BoxContextParameter::new);
	public static final ContextParameter.Type<CommandSourceContextParameter> COMMAND_SOURCE = registerInternal("command_source", CommandSourceContextParameter::new);
	public static final ContextParameter.Type<DamageSourceContextParameter> DAMAGE_SOURCE = registerInternal("damage_source", DamageSourceContextParameter::new);
	public static final ContextParameter.Type<DirectionContextParameter> DIRECTION = registerInternal("direction", DirectionContextParameter::new);
	public static final ContextParameter.Type<EffectContextParameter> EFFECT = registerInternal("effect", EffectContextParameter::new);
	public static final ContextParameter.Type<EntityContextParameter> ENTITY = registerInternal("entity", EntityContextParameter::new);
	public static final ContextParameter.Type<ItemContextParameter> ITEM = registerInternal("item", ItemContextParameter::new);
	public static final ContextParameter.Type<NbtContextParameter> NBT = registerInternal("nbt", NbtContextParameter::new);
	public static final ContextParameter.Type<FloatContextParameter> FLOAT = registerInternal("float", FloatContextParameter::new);
	public static final ContextParameter.Type<IntContextParameter> INT = registerInternal("int", IntContextParameter::new);
	public static final ContextParameter.Type<SlotContextParameter> SLOT = registerInternal("slot", SlotContextParameter::new);
	public static final ContextParameter.Type<StringContextParameter> STRING = registerInternal("string", StringContextParameter::new);
	public static final ContextParameter.Type<Vec3ContextParameter> VEC3 = registerInternal("vec3", Vec3ContextParameter::new);

	public static void registerAll() {
		ContextParameter.Type.ALIASES.addPathAlias("integer", INT);
		ContextParameter.Type.ALIASES.addPathAlias("vector_3", VEC3);
	}

	public static <P extends ContextParameter<?>> ContextParameter.Type<P> register(ResourceLocation id, Function<ResourceLocation, P> constructor) {
		return Registry.register(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, id, ContextParameter.Type.of(constructor));
	}

	private static <P extends ContextParameter<?>> ContextParameter.Type<P> registerInternal(String path, Function<ResourceLocation, P> constructor) {
		return register(NeoApoli.id(path), constructor);
	}

}
