package dev.nolij.toomanyrecipeviewers;

import dev.emi.emi.jemi.JemiPlugin;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.toomanyrecipeviewers.impl.common.config.JEIClientConfigs;
import dev.nolij.toomanyrecipeviewers.impl.common.network.ConnectionToServer;
import mezz.jei.common.Internal;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.JEIRuntimeStorage.*;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class TooManyRecipeViewersMod {
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Refraction REFRACTION = new Refraction(MethodHandles.lookup());
	
	public TooManyRecipeViewersMod(IEventBus modEventBus) {
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
		event.registerReloadListener((ResourceManagerReloadListener) (ResourceManager resourceManager) -> {
			final JEIRuntimeStorage storage = JEIRuntimeStorage.storage;
			if (storage != null && storage.resourceReloadHandler != null)
				storage.resourceReloadHandler.onResourceManagerReload(resourceManager);
		});
	}
	
}
