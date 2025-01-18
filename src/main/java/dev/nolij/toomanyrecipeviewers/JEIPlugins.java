package dev.nolij.toomanyrecipeviewers;

import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.nolij.libnolij.collect.InverseSet;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.REFRACTION;

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
	public static final Set<String> modsWithEMIPlugins =
		JemiUtil
			.getHandledMods()
			.stream()
			.filter(forceLoadJEIPluginsFrom::contains)
			.collect(Collectors.toUnmodifiableSet());
	
	public static final List<IModPlugin> allPlugins;
	public static final List<IModPlugin> modPlugins;
	public static final VanillaPlugin vanillaPlugin;
	
	static {
		final List<Class<? extends IModPlugin>> pluginClasses = getInstances(JeiPlugin.class, IModPlugin.class);
		
		pluginClasses.remove(JemiPlugin.class);
		
		// necessary ordering
		pluginClasses.remove(VanillaPlugin.class);
		pluginClasses.addFirst(VanillaPlugin.class);
		pluginClasses.remove(JeiInternalPlugin.class);
		pluginClasses.addLast(JeiInternalPlugin.class);
		
		final ArrayList<IModPlugin> plugins = new ArrayList<>();
		for (final Class<? extends IModPlugin> pluginClass : pluginClasses) {
			final IModPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
			final ResourceLocation id = plugin.getPluginUid();
			
			if (modsWithEMIPlugins.contains(id.getNamespace()))
				continue;
			
			plugins.add(plugin);
		}
		
		allPlugins = plugins;
		modPlugins = 
			plugins
				.stream()
				.filter(x -> !(x instanceof VanillaPlugin))
				.toList();
		vanillaPlugin = 
			plugins
				.stream()
				.filter(VanillaPlugin.class::isInstance)
				.map(VanillaPlugin.class::cast)
				.findFirst()
				.orElseThrow();
	}
	
	private static void dispatch(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher, boolean onMainThread) {
		final var callerMethod = new Exception().getStackTrace()[1].getMethodName();
		
		final var timestamp = System.currentTimeMillis();
		for (final var plugin : plugins) {
			final var pluginId = plugin.getPluginUid();
			final var pluginTimestamp = System.currentTimeMillis();
			try {
				if (onMainThread) {
					Minecraft.getInstance().executeBlocking(() -> dispatcher.accept(plugin));
				} else {
					dispatcher.accept(plugin);
				}
			} catch (Throwable t) {
				LOGGER.error("[{}] {} threw exception after {}ms: ", pluginId, callerMethod, System.currentTimeMillis() - pluginTimestamp, t);
			}
			LOGGER.info("[{}] {} took {}ms", pluginId, callerMethod, System.currentTimeMillis() - pluginTimestamp);
		}
		
		final var totalDispatchTime = System.currentTimeMillis() - timestamp;
		LOGGER.info("{} took {}ms total", callerMethod, totalDispatchTime);
	}
	
	public static void registerItemSubtypes(ISubtypeRegistration registration) {
		dispatch(modPlugins, x -> x.registerItemSubtypes(registration), false);
	}
	
	public static <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		dispatch(modPlugins, x -> x.registerFluidSubtypes(registration, platformFluidHelper), false);
	}
	
	public static void registerIngredients(IModIngredientRegistration registration) {
		dispatch(allPlugins, x -> x.registerIngredients(registration), false);
	}
	
	public static void registerExtraIngredients(IExtraIngredientRegistration registration) {
		dispatch(modPlugins, x -> x.registerExtraIngredients(registration), false);
	}
	
	public static void registerIngredientAliases(IIngredientAliasRegistration registration) {
		dispatch(modPlugins, x -> x.registerIngredientAliases(registration), false);
	}
	
	public static void registerModInfo(IModInfoRegistration modAliasRegistration) {
		dispatch(modPlugins, x -> x.registerModInfo(modAliasRegistration), false);
	}
	
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
		dispatch(allPlugins, x -> x.onRuntimeAvailable(jeiRuntime), false);
	}
	
	public static void onRuntimeUnavailable() {
		dispatch(allPlugins, IModPlugin::onRuntimeUnavailable, false);
	}
	
	public static void onConfigManagerAvailable(IJeiConfigManager configManager) {
		dispatch(modPlugins, x -> x.onConfigManagerAvailable(configManager), false);
	}
	
}
