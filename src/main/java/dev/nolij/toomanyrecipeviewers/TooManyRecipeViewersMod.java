package dev.nolij.toomanyrecipeviewers;

import dev.emi.emi.jemi.JemiPlugin;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.toomanyrecipeviewers.impl.common.config.JEIClientConfigs;
import mezz.jei.common.Internal;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
//? if >=21.1 {
import net.neoforged.fml.common.Mod;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
//?} else {
/*import net.minecraftforge.common.MinecraftForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
*///?}
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.*;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;

@Mod(value = MOD_ID/*? if >=21.1 {*/, dist = Dist.CLIENT /*?}*/)
public class TooManyRecipeViewersMod {
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Refraction REFRACTION = new Refraction(MethodHandles.lookup());
	
	public TooManyRecipeViewersMod(/*? if >=21.1 {*/IEventBus modEventBus/*?}*/) {
		//? if <21.1 {
		/*if (!FMLEnvironment.dist.isClient())
			return;
		
		@SuppressWarnings("removal") final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		*///?}
		
		Internal.setKeyMappings(jeiKeyMappings);
		JemiPlugin.runtime = staticJEIRuntime;
		
		Internal.setJeiClientConfigs(new JEIClientConfigs());
		
		LOGGER.info("Loading JEI Plugins: [{}]", JEIPlugins.allPlugins.stream().map(x -> x.getPluginUid().toString()).collect(Collectors.joining(", ")));
		JEIPlugins.onConfigManagerAvailable(jeiConfigManager);
		
		modEventBus.addListener(this::onRegisterClientReloadListeners);
		//? if >=21.1 {
		NeoForge
		//?} else
		/*MinecraftForge*/
			.EVENT_BUS.addListener(this::onLoggingOut);
		
		//? if >=21.1
		modEventBus.addListener(this::onRegisterPayloadHandlersEvent);
	}
	
	private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		EMIPlugin.onRuntimeUnavailable();
	}
	
	private void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(Internal.getTextures().getSpriteUploader());
	}
	
	//? if >=21.1 {
	private void onRegisterPayloadHandlersEvent(RegisterPayloadHandlersEvent event) {
		event.registrar("3")
			.executesOn(HandlerThread.MAIN)
			.optional()
			.playToServer(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC, (e, context) -> {});
	}
	//?}
	
}
