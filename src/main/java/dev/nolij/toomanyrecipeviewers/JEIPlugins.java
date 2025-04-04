package dev.nolij.toomanyrecipeviewers;

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
//? if >=21.1
import mezz.jei.api.registration.IModInfoRegistration;
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

public final class JEIPlugins {
	
	@Deprecated
	private JEIPlugins() {}
	
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
	
	private static final InverseSet<String> forceLoadJEIPluginsFrom = InverseSet.of("emi", "jei", "jei-api", MOD_ID);
	private static final Set<String> modsWithEMIPlugins =
		JemiUtil
			.getHandledMods()
			.stream()
			.filter(forceLoadJEIPluginsFrom::contains)
			.collect(Collectors.toUnmodifiableSet());
	
	public static final List<IModPlugin> allPlugins = new ArrayList<>();
	public static final List<IModPlugin> modPlugins = new ArrayList<>();
	public static final VanillaPlugin vanillaPlugin;
	
	static {
		final List<Class<? extends IModPlugin>> pluginClasses = getInstances(JeiPlugin.class, IModPlugin.class);
		
		pluginClasses.remove(JemiPlugin.class);
		
		// necessary ordering
		pluginClasses.remove(VanillaPlugin.class);
		pluginClasses.addFirst(VanillaPlugin.class);
		pluginClasses.remove(JeiInternalPlugin.class);
		pluginClasses.addLast(JeiInternalPlugin.class);
		
		for (final var pluginClass : pluginClasses) {
			final IModPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
			
			if (modsWithEMIPlugins.contains(plugin.getPluginUid().getNamespace()))
				continue;
			allPlugins.add(plugin);
			if (plugin instanceof VanillaPlugin)
				continue;
			modPlugins.add(plugin);
		}
		
		vanillaPlugin = 
			allPlugins
				.stream()
				.filter(VanillaPlugin.class::isInstance)
				.map(VanillaPlugin.class::cast)
				.findFirst()
				.orElseThrow();
	}
	
	private static final Map<IModPlugin, Long> loadTimes = Collections.synchronizedMap(new HashMap<>());
	private static long loadTime = 0L;
	
	public static void resetLoadTimes() {
		loadTimes.clear();
		loadTime = 0L;
	}
	
	public static void logLoadTimes() {
		for (final var plugin : allPlugins) {
			LOGGER.info("[{}] Loaded in {}ms", plugin.getPluginUid(), loadTimes.get(plugin));
		}
		LOGGER.info("JEI plugins loaded in {}ms", loadTime);
	}
	
	private static void dispatchInternal(IModPlugin plugin, Consumer<IModPlugin> dispatcher, String callerMethod) {
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
	
	private static void dispatchInternal(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher, String callerMethod) {
		for (final var plugin : plugins) {
			dispatchInternal(plugin, dispatcher, callerMethod);
		}
	}
	
	private static void dispatch(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher, boolean onMainThread) {
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
	
	public static void registerItemSubtypes(ISubtypeRegistration registration) {
		dispatch(modPlugins, x -> x.registerItemSubtypes(registration), false);
	}
	
	public static <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		dispatch(modPlugins, x -> x.registerFluidSubtypes(registration, platformFluidHelper), false);
	}
	
	public static void registerIngredients(IModIngredientRegistration registration) {
		dispatch(modPlugins, x -> x.registerIngredients(registration), false);
	}
	
	public static void registerExtraIngredients(IExtraIngredientRegistration registration) {
		dispatch(modPlugins, x -> x.registerExtraIngredients(registration), false);
	}
	
	public static void registerIngredientAliases(IIngredientAliasRegistration registration) {
		dispatch(modPlugins, x -> x.registerIngredientAliases(registration), false);
	}
	
	//? if >=21.1 {
	public static void registerModInfo(IModInfoRegistration modAliasRegistration) {
		dispatch(modPlugins, x -> x.registerModInfo(modAliasRegistration), false);
	}
	//?}
	
	public static void registerCategories(IRecipeCategoryRegistration registration) {
		dispatch(allPlugins, x -> x.registerCategories(registration), false);
	}
	
	public static void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		dispatch(allPlugins, x -> x.registerVanillaCategoryExtensions(registration), false);
	}
	
	public static void registerRecipes(IRecipeRegistration registration) {
		dispatch(modPlugins, x -> x.registerRecipes(registration), true);
	}
	
	public static void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		dispatch(modPlugins, x -> x.registerRecipeTransferHandlers(registration), false);
	}
	
	public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		dispatch(modPlugins, x -> x.registerRecipeCatalysts(registration), false);
	}
	
	public static void registerGuiHandlers(IGuiHandlerRegistration registration) {
		dispatch(modPlugins, x -> x.registerGuiHandlers(registration), false);
	}
	
	public static void registerAdvanced(IAdvancedRegistration registration) {
		dispatch(modPlugins, x -> x.registerAdvanced(registration), false);
	}
	
	public static void registerRuntime(IRuntimeRegistration registration) {
		dispatch(modPlugins, x -> x.registerRuntime(registration), false);
	}
	
	public static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		dispatch(modPlugins, x -> x.onRuntimeAvailable(jeiRuntime), true);
	}
	
	public static void onRuntimeUnavailable() {
		dispatch(modPlugins, IModPlugin::onRuntimeUnavailable, false);
	}
	
	public static void onConfigManagerAvailable(IJeiConfigManager configManager) {
		dispatch(modPlugins, x -> x.onConfigManagerAvailable(configManager), false);
	}
	
}
