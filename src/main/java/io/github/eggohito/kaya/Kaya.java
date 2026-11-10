package io.github.eggohito.kaya;

import io.github.eggohito.kaya.action.manager.ActionManager;
import io.github.eggohito.kaya.command.ActionCommand;
import io.github.eggohito.kaya.command.ConditionCommand;
import io.github.eggohito.kaya.command.PowerCommand;
import io.github.eggohito.kaya.command.ResourceCommand;
import io.github.eggohito.kaya.condition.manager.ConditionManager;
import io.github.eggohito.kaya.config.KayaCommonConfig;
import io.github.eggohito.kaya.duck.internal.PowerRecipeDisplayHolder;
import io.github.eggohito.kaya.duck.internal.ServerCommandStorageHolder;
import io.github.eggohito.kaya.integration.CommonConfigIntegrations;
import io.github.eggohito.kaya.integration.PowerIntegrations;
import io.github.eggohito.kaya.key.manager.KeyStateManager;
import io.github.eggohito.kaya.network.KayaServerboundPacketListener;
import io.github.eggohito.kaya.network.packet.KayaPackets;
import io.github.eggohito.kaya.network.packet.clientbound.ClientboundClearCachedLogsPacket;
import io.github.eggohito.kaya.power.global.manager.GlobalPowerSetManager;
import io.github.eggohito.kaya.power.manager.PowerManager;
import io.github.eggohito.kaya.registry.*;
import io.github.eggohito.kaya.registry.area.KayaAreaTypes;
import io.github.eggohito.kaya.registry.attachment.KayaEntityAttachments;
import io.github.eggohito.kaya.registry.context.KayaContextParameterTypes;
import io.github.eggohito.kaya.registry.context.KayaContextParameters;
import io.github.eggohito.kaya.registry.provider.KayaValueProviderKinds;
import io.github.eggohito.kaya.registry.provider.KayaValueProviderTypes;
import io.github.eggohito.kaya.registry.recipe.KayaRecipeBookCategories;
import io.github.eggohito.kaya.registry.recipe.KayaRecipeSerializers;
import io.github.eggohito.kaya.tag.manager.NestedTagManager;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Objects;

public final class Kaya implements ModInitializer {

	public static final String MOD_NAMESPACE = "kaya";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAMESPACE);

	static final IntSet CACHED_LOGS = new IntOpenHashSet();

	static MinecraftServer server;
	static Version version;

	static boolean embedded;

	@Override
	public void onInitialize() {

		CommonConfigIntegrations.init();
		ModContainer mod = FabricLoader.getInstance()
			.getModContainer(MOD_NAMESPACE)
			.orElseThrow();

		version = mod.getMetadata().getVersion();
		embedded = mod.getContainingMod().isPresent();

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {

			var rootNode = dispatcher.getRoot();
			var baseNode = Commands.literal(MOD_NAMESPACE).build();

			PowerCommand.register(rootNode);
			PowerCommand.register(baseNode);

			ActionCommand.register(buildContext, rootNode);
			ActionCommand.register(buildContext, baseNode);

			ConditionCommand.register(buildContext, rootNode);
			ConditionCommand.register(buildContext, baseNode);

			ResourceCommand.register(rootNode);
			ResourceCommand.register(baseNode);

			rootNode.addChild(baseNode);

		});

		KayaPackets.registerAll();
		KayaServerboundPacketListener.init();

		KayaContextParameterTypes.registerAll();

		KayaValueProviderKinds.registerAll();
		KayaValueProviderTypes.registerAll();

		KayaArgumentTypes.registerAll();
		KayaComponentContents.registerAll();
		KayaContainerMenuTypes.registerAll();
		KayaParticleTypes.registerAll();
		KayaDataProviders.registerAll();

		KayaRecipeSerializers.registerAll();
		KayaRecipeBookCategories.registerAll();

		KayaEntityAttachments.registerAll();

		KayaAreaTypes.registerAll();
		KayaComparisonTypes.registerAll();
		KayaColorTypes.registerAll();
		KayaModifierTypes.registerAll();
		KayaHudElementTypes.registerAll();

		KayaActionTypes.registerAll();
		KayaConditionTypes.registerAll();
		KayaPowerTypes.registerAll();

		ActionManager.getInstance().init();
		ConditionManager.getInstance().init();
		GlobalPowerSetManager.getInstance().init();
		KeyStateManager.getInstance().init();
		NestedTagManager.getInstance().init();
		PowerManager.getInstance().init();

		PowerIntegrations.init();
		KayaContextParameters.registerAll();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> Kaya.server = server);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> Kaya.server = null);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ((ServerCommandStorageHolder) server).kaya$sendAll(handler.getPlayer()));
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> ((PowerRecipeDisplayHolder) player.server.getRecipeManager()).kaya$sendAll(player));

		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
			CACHED_LOGS.clear();
			server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, ClientboundClearCachedLogsPacket.INSTANCE));
		});

	}

	public static Version getVersion() {
		return Objects.requireNonNull(version, MOD_NAMESPACE + " wasn't initialized yet!");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_NAMESPACE, path);
	}

	@SuppressWarnings("UnstableApiUsage")
	public static boolean performStandaloneHandshake() {
		return KayaCommonConfig.INSTANCE.performHandshake.get().toBoolean(!embedded);
	}

	public static boolean onServerThread() {
		return server != null
			&& server.isSameThread();
	}

	public static void logOnce(Level level, String message) {

		if (CACHED_LOGS.add(message.hashCode())) {
			LOGGER.atLevel(level).log(message);
		}

	}

}
