package dev.nolij.toomanyrecipeviewers;

import dev.emi.emi.jemi.JemiPlugin;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.toomanyrecipeviewers.impl.common.config.JEIClientConfigs;
import mezz.jei.common.Internal;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.*;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class TooManyRecipeViewersMod {
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Refraction REFRACTION = new Refraction(MethodHandles.lookup());
	
	public TooManyRecipeViewersMod(IEventBus modEventBus) {
		Internal.setKeyMappings(jeiKeyMappings);
		JemiPlugin.runtime = staticJEIRuntime;
		
		Internal.setJeiClientConfigs(new JEIClientConfigs());
		
		LOGGER.info("Loading JEI Plugins: [{}]", JEIPlugins.allPlugins.stream().map(x -> x.getPluginUid().toString()).collect(Collectors.joining(", ")));
		JEIPlugins.onConfigManagerAvailable(jeiConfigManager);
		
		modEventBus.addListener(this::onRegisterClientReloadListeners);
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> EMIPlugin.onRuntimeUnavailable());
		
		modEventBus.addListener(this::onRegisterPayloadHandlersEvent);
	}
	
	private void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(Internal.getTextures().getSpriteUploader());
	}
	
	private void onRegisterPayloadHandlersEvent(RegisterPayloadHandlersEvent event) {
		event.registrar("3")
			.executesOn(HandlerThread.MAIN)
			.optional()
			.playToServer(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC, (e, context) -> {});
	}
	
}
