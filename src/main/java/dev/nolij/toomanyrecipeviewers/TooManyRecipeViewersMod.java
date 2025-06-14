package dev.nolij.toomanyrecipeviewers;

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
import dev.emi.emi.jemi.JemiPlugin;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.toomanyrecipeviewers.impl.jei.common.config.JEIClientConfigs;
import dev.nolij.toomanyrecipeviewers.impl.jei.common.network.ConnectionToServer;
import mezz.jei.common.Internal;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
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
			.playToServer(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC, (recipeTransferPacket, context) -> {
				// We never process this packet. On a multiplayer server, TMRV is never loaded, so there are two possibilities:
				// - JEI is installed. In this case JEI will register a handler for this packet on the server.
				// - EMI is installed. In this case TMRV is not loaded, so the server will not support this packet,
				//   and TMRV will fall back to the EMI packet.
				// We special-case singleplayer and never send this packet (since we don't have logic for handling it),
				// so nothing needs to be processed here.
			});
	}
	//?}
	
}
