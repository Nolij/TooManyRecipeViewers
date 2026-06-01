package dev.nolij.toomanyrecipeviewers;

//? if >=21.1 {
import mezz.jei.api.registration.IModInfoRegistration;
//?}
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.nolij.libnolij.collect.InverseSet;
import dev.nolij.libnolij.collect.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
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
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
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
	private static final List<Class<? extends IModPlugin>> pluginClasses = getInstances(JeiPlugin.class, IModPlugin.class);
	private static final List<Pair<ResourceLocation, IModPlugin>> unfilteredPlugins = new ArrayList<>();
	
	static {
		pluginClasses.remove(JemiPlugin.class);
		
		// necessary ordering
		pluginClasses.remove(VanillaPlugin.class);
		pluginClasses.remove(JeiInternalPlugin.class);
		pluginClasses.addLast(JeiInternalPlugin.class);
		
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
			
			unfilteredPlugins.add(Pair.of(pluginID, plugin));
		}
	}
	
	private static final InverseSet<String> forceLoadJEIPluginsFrom = InverseSet.of("emi", "jei", "jei-api", MOD_ID);
	private static final Set<String> modsWithEMIPlugins =
		JemiUtil
			.getHandledMods()
			.stream()
			.filter(forceLoadJEIPluginsFrom::contains)
			.collect(Collectors.toUnmodifiableSet());
	
	private final ForkJoinPool dispatchPool = new ForkJoinPool();
	
	public final List<IModPlugin> allPlugins = new ArrayList<>(pluginClasses.size());
	public final List<IModPlugin> modPluginsNoDuplicates = new ArrayList<>(pluginClasses.size());
	public final List<IModPlugin> modPlugins = new ArrayList<>(pluginClasses.size());
	public final Set<IModPlugin> partialLoadPlugins = new HashSet<>(pluginClasses.size());
	public final VanillaPlugin vanillaPlugin = new VanillaPlugin();
	public final String pluginListString;
	
	private final Object2LongMap<IModPlugin> loadTimes = new Object2LongOpenHashMap<>(pluginClasses.size());
	private long loadTime = 0L;
	
	// must run after all other EMI plugins are initialized
	// ∴ require passing EmiRegistry to avoid future footguns
	JEIPluginManager(EmiRegistry ignored) {
		final var additionalModIDs = EmiRecipes.categories.stream()
			.map(x -> x.getId().getNamespace())
			.collect(Collectors.toSet());
		
		var pluginListStringBuilder = new StringBuilder(vanillaPlugin.getPluginUid().toString());
		
		allPlugins.add(vanillaPlugin);
		for (final var pluginWithID : unfilteredPlugins) {
			final var pluginID = pluginWithID.value1();
			final var plugin = pluginWithID.value2();
			
			allPlugins.add(plugin);
			modPlugins.add(plugin);
			pluginListStringBuilder.append(", ");
			pluginListStringBuilder.append(pluginID);
			if (modsWithEMIPlugins.contains(pluginID.getNamespace())) {
				pluginListStringBuilder.append("¹");
				partialLoadPlugins.add(plugin);
			} else if (additionalModIDs.contains(pluginID.getNamespace())) {
				pluginListStringBuilder.append("²");
				partialLoadPlugins.add(plugin);
			} else {
				modPluginsNoDuplicates.add(plugin);
			}
		}
		
		pluginListString = pluginListStringBuilder.toString();
	}
	
	public void logLoadTimes() {
		for (final var plugin : allPlugins) {
			LOGGER.info("[{}] Loaded in {}ms", plugin.getPluginUid(), loadTimes.getLong(plugin));
		}
		LOGGER.info("JEI plugins loaded in {}ms", loadTime);
	}
	
	private enum DispatchStrategy {
		SYNC_EMI, SYNC_MAIN, ASYNC
	}
	
	private record DispatchResult(IModPlugin plugin, long duration, @Nullable Throwable exception) {}
	
	private record DispatchThread(IModPlugin plugin, Consumer<IModPlugin> dispatcher) implements Callable<DispatchResult> {
		
		@Override
		public DispatchResult call() {
			final var timestamp = System.currentTimeMillis();
			
			try {
				dispatcher.accept(plugin);
				return new DispatchResult(plugin, System.currentTimeMillis() - timestamp, null);
			} catch (Throwable t) {
				return new DispatchResult(plugin, System.currentTimeMillis() - timestamp, t);
			}
		}
		
	}
	
	private void singleThreadedDispatch(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher, String callerMethod) {
		for (final var plugin : plugins) {
			final var pluginId = plugin.getPluginUid();
			
			final var result = new DispatchThread(plugin, dispatcher).call();
			
			if (result.exception != null) {
				LOGGER.error("[{}] {} threw exception after {}ms: ", pluginId, callerMethod, result.duration(), result.exception());
			}
			
			loadTimes.put(plugin, loadTimes.getOrDefault(plugin, 0L) + result.duration());
		}
	}
	
	private void multiThreadedDispatch(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher) {
		final var results = dispatchPool
			.invokeAll(plugins.stream()
				.map(plugin -> new DispatchThread(plugin, dispatcher))
				.toList())
			.stream()
			.map(Future::get)
			.toList();
		
		for (final var result : results) {
			loadTimes.put(result.plugin(), loadTimes.getOrDefault(result.plugin(), 0L) + result.duration());
		}
	}
	
	private void dispatch(List<IModPlugin> plugins, Consumer<IModPlugin> dispatcher, DispatchStrategy dispatchStrategy, long worry) {
		final var callerMethod = new Exception().getStackTrace()[1].getMethodName();
		
		EmiReloadManager.step(Component.literal("[TMRV] Dispatching `%s` with strategy `%s`...".formatted(callerMethod, dispatchStrategy.name())), worry);
		final var timestamp = System.currentTimeMillis();
		
		// TODO: handle strategy on per-plugin basis
		switch (dispatchStrategy) {
			case SYNC_EMI -> singleThreadedDispatch(plugins, dispatcher, callerMethod);
			case SYNC_MAIN -> Minecraft.getInstance().executeBlocking(() -> singleThreadedDispatch(plugins, dispatcher, callerMethod));
			case ASYNC -> multiThreadedDispatch(plugins, dispatcher);
		}
		
		final var totalDispatchTime = System.currentTimeMillis() - timestamp;
		// TODO: track + output number of exceptions
		EmiReloadManager.step(Component.literal("[TMRV] Dispatching `%s` with strategy `%s` took %dms".formatted(callerMethod, dispatchStrategy.name(), totalDispatchTime)));
		loadTime += totalDispatchTime;
	}
	
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerItemSubtypes(registration), DispatchStrategy.ASYNC, 10L);
	}
	
	public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		dispatch(modPluginsNoDuplicates, x -> x.registerFluidSubtypes(registration, platformFluidHelper), DispatchStrategy.SYNC_EMI, 10L);
	}
	
	public void registerIngredients(IModIngredientRegistration registration) {
		dispatch(modPlugins, x -> x.registerIngredients(registration), DispatchStrategy.SYNC_EMI, 10L);
	}
	
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerExtraIngredients(registration), DispatchStrategy.ASYNC, 10L);
	}
	
	public void registerIngredientAliases(IIngredientAliasRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerIngredientAliases(registration), DispatchStrategy.ASYNC, 10L);
	}
	
	//? if >=21.1 {
	public void registerModInfo(IModInfoRegistration modAliasRegistration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerModInfo(modAliasRegistration), DispatchStrategy.SYNC_EMI, 10L);
	}
	//?}
	
	public void registerCategories(IRecipeCategoryRegistration registration) {
		dispatch(allPlugins, x -> x.registerCategories(registration), DispatchStrategy.ASYNC, 100L);
	}
	
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		dispatch(allPlugins, x -> x.registerVanillaCategoryExtensions(registration), DispatchStrategy.ASYNC, 100L);
	}
	
	public void registerRecipes(IRecipeRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRecipes(registration), DispatchStrategy.ASYNC, 500L);
	}
	
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRecipeTransferHandlers(registration), DispatchStrategy.SYNC_EMI, 10L);
	}
	
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRecipeCatalysts(registration), DispatchStrategy.SYNC_EMI, 10L);
	}
	
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerGuiHandlers(registration), DispatchStrategy.ASYNC, 10L);
	}
	
	public void registerAdvanced(IAdvancedRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerAdvanced(registration), DispatchStrategy.SYNC_EMI, 10L);
	}
	
	public void registerRuntime(IRuntimeRegistration registration) {
		dispatch(modPluginsNoDuplicates, x -> x.registerRuntime(registration), DispatchStrategy.SYNC_EMI, 10L);
	}
	
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		dispatch(modPluginsNoDuplicates, x -> x.onRuntimeAvailable(jeiRuntime), DispatchStrategy.SYNC_EMI, 100L);
	}
	
	public void onRuntimeUnavailable() {
		dispatch(modPluginsNoDuplicates, IModPlugin::onRuntimeUnavailable, DispatchStrategy.SYNC_EMI, 10L);
	}
	
	public void onConfigManagerAvailable(IJeiConfigManager configManager) {
		dispatch(modPluginsNoDuplicates, x -> x.onConfigManagerAvailable(configManager), DispatchStrategy.SYNC_EMI, 10L);
	}
	
}
