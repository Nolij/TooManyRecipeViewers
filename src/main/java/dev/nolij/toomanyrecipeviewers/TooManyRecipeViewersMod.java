package dev.nolij.toomanyrecipeviewers;

import dev.emi.emi.jemi.JemiPlugin;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.toomanyrecipeviewers.impl.common.config.JEIClientConfigs;
import dev.nolij.toomanyrecipeviewers.impl.common.network.ConnectionToServer;
import mezz.jei.common.Internal;
//? if neoforge {
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
//?}
//? if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*///?}
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.*;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;

@Mod(value = MOD_ID/*? if neoforge {*/, dist = Dist.CLIENT/*?}*/)
public class TooManyRecipeViewersMod {
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Refraction REFRACTION = new Refraction(MethodHandles.lookup());
	
	public TooManyRecipeViewersMod(/*? if neoforge {*/IEventBus modEventBus/*?}*/) {
		//? if forge
		/*IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();*/

		Internal.setKeyMappings(jeiKeyMappings);
		JemiPlugin.runtime = staticJEIRuntime;
		
		Internal.setServerConnection(new ConnectionToServer());
		Internal.setJeiClientConfigs(new JEIClientConfigs());
		
		LOGGER.info("Loading JEI Plugins: [{}]", JEIPlugins.allPlugins.stream().map(x -> x.getPluginUid().toString()).collect(Collectors.joining(", ")));
		JEIPlugins.onConfigManagerAvailable(jeiConfigManager);
		
		modEventBus.addListener(this::onRegisterClientReloadListeners);
	}
	
	private void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(Internal.getTextures().getSpriteUploader());
	}
	
}
