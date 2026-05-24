package dev.nolij.toomanyrecipeviewers;

//? if >=21.1
import mezz.jei.api.registration.IModInfoRegistration;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.nolij.libnolij.collect.InverseSet;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import mezz.jei.library.plugins.jei.JeiInternalPlugin;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.*;

public final class JEIPluginManager {
	
	@SuppressWarnings("SameParameterValue")
	private static <T> List<Class<? extends T>> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
		final var annotationType = Type.getType(annotationClass);
		
		final List<Class<? extends T>> result = new ArrayList<>();
		for (final var scanData : ModList.get().getAllScanData()) {
			for (final var annotation : scanData.getAnnotations()) {
				if (Objects.equals(annotation.annotationType(), annotationType)) {
					result.add(Objects.requireNonNull(REFRACTION.getClassOrNull(annotation.memberName())).asSubclass(instanceClass));
				}
			}
		}
		
		return result;
	}
	private static final List<Class<? extends IModPlugin>> pluginClasses = getInstances(JeiPlugin.class, IModPlugin.class);;
	
	static {
		pluginClasses.remove(JemiPlugin.class);
		
		// necessary ordering
		pluginClasses.remove(VanillaPlugin.class);
		pluginClasses.remove(JeiInternalPlugin.class);
		pluginClasses.addLast(JeiInternalPlugin.class);
	}
	
	private static final InverseSet<String> forceLoadJEIPluginsFrom = InverseSet.of("emi", "jei", "jei-api", MOD_ID);
	private static final Set<String> modsWithEMIPlugins =
		JemiUtil
			.getHandledMods()
			.stream()
			.filter(forceLoadJEIPluginsFrom::contains)
			.collect(Collectors.toUnmodifiableSet());
	
	public final List<IModPlugin> allPlugins = new ArrayList<>(pluginClasses.size());
	public final List<IModPlugin> modPluginsNoDuplicates = new ArrayList<>(pluginClasses.size());
	public final List<IModPlugin> modPlugins = new ArrayList<>(pluginClasses.size());
	public final VanillaPlugin vanillaPlugin = new VanillaPlugin();
	public final String pluginListString;
	
	private final Map<IModPlugin, Long> loadTimes = Collections.synchronizedMap(new HashMap<>(pluginClasses.size()));
	private long loadTime = 0L;
	
	// must run after all other EMI plugins are initialized
	// ∴ require passing EmiRegistry to avoid future footguns
	JEIPluginManager(EmiRegistry ignored) {
		final var additionalModIDs = EmiRecipes.categories.stream()
			.map(x -> x.getId().getNamespace())
			.collect(Collectors.toSet());
		
		var pluginListStringBuilder = new StringBuilder(vanillaPlugin.getPluginUid().toString());
		
		allPlugins.add(vanillaPlugin);
		for (final var pluginClass : pluginClasses) {
			final IModPlugin plugin;
			try {
				plugin = pluginClass.getDeclaredConstructor().newInstance();
			} catch (Throwable t) {
				LOGGER.error("Failed to initialize JEI plugin {}", pluginClass.getName(), t);
				continue;
			}
			
			final ResourceLocation pluginID;
			try {
				pluginID = Objects.requireNonNull(plugin.getPluginUid());
			} catch (Throwable t) {
				LOGGER.error("{}.getPluginUid() threw an exception or returned null", pluginClass.getName(), t);
				continue;
			}
			
			allPlugins.add(plugin);
			modPlugins.add(plugin);
			pluginListStringBuilder.append(", ");
			pluginListStringBuilder.append(pluginID);
			if (modsWithEMIPlugins.contains(pluginID.getNamespace())) {
				pluginListStringBuilder.append("¹");
			} else if (additionalModIDs.contains(pluginID.getNamespace())) {
				pluginListStringBuilder.append("²");
			} else {
				modPluginsNoDuplicates.add(plugin);
			}
		}
		
		pluginListString = pluginListStringBuilder.toString();
	}
	
	public void logLoadTimes() {
		for (final var plugin : allPlugins) {
			LOGGER.info("[{}] Loaded in {}ms", plugin.getPluginUid(), loadTimes.get(plugin));
		}
		LOGGER.info("JEI plugins loaded in {}ms", loadTime);
	}
	
	private void dispatchInternal(IModPlugin plugin, Consumer<IModPlugin> dispatcher, String callerMethod) {
		final var pluginId = plugin.getPluginUid();
		final var pluginTimestamp = System.currentTimeMillis();
		long dispatchTime;
		try {
			EmiReloadManager.step(Component.literal("[TMRV] %s: %s...".formatted(callerMethod, pluginId.toString())));
			dispatcher.accept(plugin);
			dispatchTime = System.currentTimeMillis() - pluginTimestamp;
			EmiReloadManager.step(Component.literal("[TMRV] %s: %s took %dms".formatted(callerMethod, pluginId.toString(), dispatchTime)));
		} catch (Throwable t) {
			dispatchTime = System.currentTimeMillis() - pluginTimestamp;
			LOGGER.error("[{}] {} threw exception after {}ms: ", pluginId, callerMethod, dispatchTime, t);
		}
		
		loadTimes.put(plugin, loadTimes.computeIfAbsent(plugin, x -> 0L) + dispatchTime);
	}
	
	private void dispatchInternal(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher, String callerMethod) {
		for (final var plugin : plugins) {
			dispatchInternal(plugin, dispatcher, callerMethod);
		}
	}
	
	private void dispatch(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher, boolean onMainThread) {
		final var callerMethod = new Exception().getStackTrace()[1].getMethodName();
		
		final var timestamp = System.currentTimeMillis();
		
		if (onMainThread) {
			Minecraft.getInstance().executeBlocking(() -> dispatchInternal(plugins, dispatcher, callerMethod));
		} else {
			dispatchInternal(plugins, dispatcher, callerMethod);
		}
		
		final var totalDispatchTime = System.currentTimeMillis() - timestamp;
		LOGGER.info("{} took {}ms", callerMethod, totalDispatchTime);
		loadTime += totalDispatchTime;
	}
	
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerItemSubtypes(registration), false);
	}
	
	public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		dispatch(modPluginsNoDuplicates, x -> x.registerFluidSubtypes(registration, platformFluidHelper), false);
	}
	
	public void registerIngredients(IModIngredientRegistration registration) {
		dispatch(modPlugins, x -> x.registerIngredients(registration), false);
	}
	
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerExtraIngredients(registration), false);
	}
	
	public void registerIngredientAliases(IIngredientAliasRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerIngredientAliases(registration), false);
	}
	
	//? if >=21.1 {
	public void registerModInfo(IModInfoRegistration modAliasRegistration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerModInfo(modAliasRegistration), false);
	}
	//?}
	
	public void registerCategories(IRecipeCategoryRegistration registration) {
		dispatch(allPlugins, x -> x.registerCategories(registration), false);
	}
	
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		dispatch(allPlugins, x -> x.registerVanillaCategoryExtensions(registration), false);
	}
	
	public void registerRecipes(IRecipeRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRecipes(registration), true);
	}
	
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRecipeTransferHandlers(registration), false);
	}
	
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRecipeCatalysts(registration), false);
	}
	
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerGuiHandlers(registration), false);
	}
	
	public void registerAdvanced(IAdvancedRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerAdvanced(registration), false);
	}
	
	public void registerRuntime(IRuntimeRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRuntime(registration), false);
	}
	
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		dispatch(modPluginsNoDuplicates, x -> x.onRuntimeAvailable(jeiRuntime), true);
	}
	
	public void onRuntimeUnavailable() {
		dispatch(modPluginsNoDuplicates, IModPlugin::onRuntimeUnavailable, false);
	}
	
	public void onConfigManagerAvailable(IJeiConfigManager configManager) {
		dispatch(modPluginsNoDuplicates, x -> x.onConfigManagerAvailable(configManager), false);
	}
	
}
