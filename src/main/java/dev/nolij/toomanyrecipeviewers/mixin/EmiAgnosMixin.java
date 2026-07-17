package dev.nolij.toomanyrecipeviewers.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.nolij.toomanyrecipeviewers.plugin.TMRVPluginConfig;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.PLUGIN_CONFIG_FILE;

@Mixin(value = EmiAgnos.class, remap = false)
public class EmiAgnosMixin {
	
	@WrapMethod(method = "isModLoaded")
	private static boolean tmrv$isModLoaded(String id, Operation<Boolean> original) {
		if (id.equals("jei"))
			return false; // suppress JEMI
		
		return original.call(id);
	}
	
	@WrapMethod(method = "getPlugins")
	private static List<EmiPluginContainer> tmrv$getPlugins(Operation<List<EmiPluginContainer>> original) {
		final var config = TMRVPluginConfig.readOrCreate(PLUGIN_CONFIG_FILE);
		
		final var plugins = original.call();
		
		if (config.blockedEMIPluginModIDs.isEmpty())
			return plugins;
		
		LOGGER.error("User has configured `blockedEMIPluginModIDs`. Support will not be provided! Do not report errors caused by using this setting!");
		
		return plugins.stream()
			.filter(x -> !config.blockedEMIPluginModIDs.contains(x.id()))
			.toList();
	}
	
}
