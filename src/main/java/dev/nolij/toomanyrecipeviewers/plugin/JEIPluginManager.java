package dev.nolij.toomanyrecipeviewers.plugin;

//? if >=21.1 {
import mezz.jei.api.registration.IModInfoRegistration;
//?}
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.nolij.libnolij.collect.InverseSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
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
import mezz.jei.library.plugins.jei.JeiInternalPlugin;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.jeiConfigManager;
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
	
	public static void init() {}
	
	private record LocatedPlugin(ResourceLocation id, IModPlugin plugin) {}
	private static final List<LocatedPlugin> unfilteredPlugins = new ArrayList<>();
	
	public static final VanillaPlugin vanillaPlugin = new VanillaPlugin();
	
	static {
		pluginClasses.remove(JemiPlugin.class);
		
		// necessary ordering
		pluginClasses.remove(VanillaPlugin.class);
		pluginClasses.remove(JeiInternalPlugin.class);
		pluginClasses.addLast(JeiInternalPlugin.class);
		
		unfilteredPlugins.add(new LocatedPlugin(vanillaPlugin.getPluginUid(), vanillaPlugin));
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
			
			unfilteredPlugins.add(new LocatedPlugin(pluginID, plugin));
			
			plugin.onConfigManagerAvailable(jeiConfigManager);
		}
	}
	
	private static final InverseSet<String> forceLoadJEIPluginsFrom = InverseSet.of("emi", "jei", "jei-api", MOD_ID);
	private static final Set<String> modsWithEMIPlugins =
		JemiUtil
			.getHandledMods()
			.stream()
			.filter(forceLoadJEIPluginsFrom::contains)
			.collect(Collectors.toUnmodifiableSet());
	
	public record ThreadContext(Plugin plugin, String phase) {}
	public static final ThreadLocal<@Nullable ThreadContext> threadContext = ThreadLocal.withInitial(() -> null);
	
	private final ForkJoinPool dispatchPool = new ForkJoinPool();
	
	private final List<Plugin> plugins = new ArrayList<>(unfilteredPlugins.size());
	private final Map<String, DispatchStrategy> defaultPhaseOverrides;
	private final Map<String, Map<Plugin, DispatchStrategy>> pluginPhaseOverrides = new Object2ReferenceOpenHashMap<>();
	private final boolean allowDefaultAsync;
	
	private final Reference2LongMap<Plugin> loadTimes = new Reference2LongOpenHashMap<>(unfilteredPlugins.size());
	private long loadTime = 0L;
	public final String pluginListString;
	
	// must run after all other EMI plugins are initialized
	// ∴ require passing EmiRegistry to avoid future footguns
	JEIPluginManager(EmiRegistry ignored) {
		final var config = TMRVPluginConfig.readOrCreate(PLUGIN_CONFIG_FILE);
		
		allowDefaultAsync = Objects.requireNonNullElse(config.allowDefaultAsync, true);
		defaultPhaseOverrides = config.defaultPhaseOverrides;
		
		final var registeredEMINamespaces = EmiRecipes.categories.stream()
			.map(x -> x.getId().getNamespace())
			.filter(x -> !x.equals("minecraft"))
			.collect(Collectors.toSet());
		
		for (final var locatedPlugin : unfilteredPlugins) {
			final var id = locatedPlugin.id();
			final var object = locatedPlugin.plugin();
			
			final PluginType pluginType;
			if (object instanceof VanillaPlugin) {
				pluginType = PluginType.VANILLA_PLUGIN;
			} else if (object instanceof JeiInternalPlugin) {
				pluginType = PluginType.JEI_INTERNAL_PLUGIN;
			} else if (config.typeOverrides.containsKey(id)) {
				pluginType = config.typeOverrides.get(id);
			} else if (modsWithEMIPlugins.contains(id.getNamespace())) {
				pluginType = config.defaultDisableDuplicatePlugins 
					? PluginType.DISABLED
					: PluginType.DUPLICATE_MODID;
			} else if (registeredEMINamespaces.contains(id.getNamespace())) {
				pluginType = config.defaultDisableDuplicatePlugins
					? PluginType.DISABLED
					: PluginType.DUPLICATE_NAMESPACE;
			} else {
				pluginType = PluginType.NORMAL;
			}
			
			final var hasOverrides = config.pluginPhaseOverrides.containsKey(id);
			
			final var plugin = new Plugin(id, pluginType, hasOverrides, object);
			
			if (hasOverrides) {
				final var overrides = config.pluginPhaseOverrides.get(id);
				for (final var entry : overrides.entrySet()) {
					pluginPhaseOverrides.computeIfAbsent(entry.getKey(), _ -> new Reference2ObjectOpenHashMap<>()).put(plugin, entry.getValue());
				}
			}
			
			plugins.add(plugin);
		}
		
		pluginListString = String.join(", ", plugins.stream().map(Plugin::toString).toList());
	}
	
	public void logLoadTimes() {
		for (final var plugin : plugins) {
			if (plugin.type() == PluginType.DISABLED)
				continue;
			
			LOGGER.info("[{}] Loaded in {}ms", plugin, loadTimes.getLong(plugin));
		}
		LOGGER.info("JEI plugins loaded in {}ms", loadTime);
	}
	
	private record DispatchResult(Plugin plugin, String phase, long duration, @Nullable Throwable exception) {}
	
	private record DispatchThread(Plugin plugin, String phase, Consumer<IModPlugin> dispatcher) implements Callable<DispatchResult> {
		
		@Override
		public DispatchResult call() {
			final var timestamp = System.currentTimeMillis();
			
			try {
				threadContext.set(new ThreadContext(plugin, phase));
				
				dispatcher.accept(plugin.object());
				return new DispatchResult(plugin, phase, System.currentTimeMillis() - timestamp, null);
			} catch (Throwable t) {
				return new DispatchResult(plugin, phase, System.currentTimeMillis() - timestamp, t);
			} finally {
				threadContext.remove();
			}
		}
		
	}
	
	private List<DispatchResult> singleThreadedDispatch(List<Plugin> plugins, String phase, Consumer<IModPlugin> dispatcher) {
		final var result = new ArrayList<DispatchResult>(plugins.size());
		
		for (final var plugin : plugins) {
			result.add(new DispatchThread(plugin, phase, dispatcher).call());
		}
		
		return result;
	}
	
	private synchronized void dispatch(Consumer<IModPlugin> dispatcher, DispatchStrategy defaultDispatchStrategy, boolean skipVanillaPlugin, boolean skipPartialLoad, long worry) {
		final var phase = new Exception().getStackTrace()[1].getMethodName();
		
		if (defaultPhaseOverrides.containsKey(phase))
			defaultDispatchStrategy = defaultPhaseOverrides.get(phase);
		else
			defaultDispatchStrategy = defaultDispatchStrategy.downgrade(allowDefaultAsync, DispatchStrategy.SYNC_EMI);
		
		EmiReloadManager.step(Component.literal("[TMRV] Dispatching `%s` with default strategy `%s`...".formatted(phase, defaultDispatchStrategy)), worry);
		final var timestamp = System.currentTimeMillis();
		
		final var syncEMIPlugins = new ArrayList<Plugin>();
		final var syncMainPlugins = new ArrayList<Plugin>();
		final var asyncPlugins = new ArrayList<Plugin>();
		
		final var overrides = pluginPhaseOverrides.computeIfAbsent(phase, _ -> Collections.emptyMap());
		
		var pluginCount = 0;
		for (final var plugin : plugins) {
			if (plugin.type() == PluginType.DISABLED || 
				(skipVanillaPlugin && plugin.type() == PluginType.VANILLA_PLUGIN))
				continue;
			
			final var defaultStrategy = (skipPartialLoad && plugin.type().partialLoad) 
				? DispatchStrategy.SKIP 
				: defaultDispatchStrategy;
			
			final var strategy = overrides.getOrDefault(plugin, defaultStrategy);
			
			if (strategy == DispatchStrategy.SKIP)
				continue;
			
			switch (strategy) {
				case SYNC_EMI -> syncEMIPlugins.add(plugin);
				case SYNC_MAIN -> syncMainPlugins.add(plugin);
				case ASYNC -> asyncPlugins.add(plugin);
				default -> throw new UnsupportedOperationException();
			}
			
			pluginCount++;
		}
		
		final var results = new ArrayList<DispatchResult>(pluginCount);
		
		final List<ForkJoinTask<DispatchResult>> asyncTasks;
		if (asyncPlugins.isEmpty()) {
			asyncTasks = null;
		} else {
			asyncTasks = new ArrayList<>();
			for (final var plugin : asyncPlugins) {
				asyncTasks.add(dispatchPool.submit(new DispatchThread(plugin, phase, dispatcher)));
			}
		}
		
		final CompletableFuture<List<DispatchResult>> mainThreadDispatch;
		if (syncMainPlugins.isEmpty()) {
			mainThreadDispatch = null;
		} else {
			mainThreadDispatch = Minecraft.getInstance().submit(() -> singleThreadedDispatch(syncMainPlugins, phase, dispatcher));
		}
		
		if (!syncEMIPlugins.isEmpty()) {
			results.addAll(singleThreadedDispatch(syncEMIPlugins, phase, dispatcher));
		}
		
		if (mainThreadDispatch != null) {
			results.addAll(mainThreadDispatch.join());
		}
		
		if (asyncTasks != null) {
			for (final var task : asyncTasks) {
				results.add(task.get());
			}
		}
		
		for (final var result : results) {
			if (result.exception() != null) {
				LOGGER.error("[{}] {} threw exception after {}ms: ", result.plugin(), result.phase(), result.duration(), result.exception());
			}
			
			loadTimes.put(result.plugin(), loadTimes.getOrDefault(result.plugin(), 0L) + result.duration());
		}
		
		final var totalDispatchTime = System.currentTimeMillis() - timestamp;
		EmiReloadManager.step(Component.literal("[TMRV] Dispatching `%s` took %dms".formatted(phase, totalDispatchTime)));
		loadTime += totalDispatchTime;
	}
	
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		dispatch(x -> x.registerItemSubtypes(registration), DispatchStrategy.ASYNC, true, false, 50L);
	}
	
	public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		dispatch(x -> x.registerFluidSubtypes(registration, platformFluidHelper), DispatchStrategy.ASYNC, true, false, 10L);
	}
	
	public void registerIngredients(IModIngredientRegistration registration) {
		dispatch(x -> x.registerIngredients(registration), DispatchStrategy.ASYNC, true, false, 100L);
	}
	
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		dispatch(x -> x.registerExtraIngredients(registration), DispatchStrategy.ASYNC, true, true, 50L);
	}
	
	public void registerIngredientAliases(IIngredientAliasRegistration registration) {
		dispatch(x -> x.registerIngredientAliases(registration), DispatchStrategy.ASYNC, true, true, 10L);
	}
	
	//? if >=21.1 {
	public void registerModInfo(IModInfoRegistration modAliasRegistration) {
		dispatch(x -> x.registerModInfo(modAliasRegistration), DispatchStrategy.SYNC_EMI, true, true, 10L);
	}
	//?}
	
	public void registerCategories(IRecipeCategoryRegistration registration) {
		dispatch(x -> x.registerCategories(registration), DispatchStrategy.SYNC_EMI, false, false, 100L);
	}
	
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		dispatch(x -> x.registerVanillaCategoryExtensions(registration), DispatchStrategy.SYNC_EMI, false, false, 100L);
	}
	
	public void registerRecipes(IRecipeRegistration registration) {
		dispatch(x -> x.registerRecipes(registration), DispatchStrategy.ASYNC, true, true, 500L);
	}
	
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		dispatch(x -> x.registerRecipeTransferHandlers(registration), DispatchStrategy.SYNC_EMI, true, true, 10L);
	}
	
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		dispatch(x -> x.registerRecipeCatalysts(registration), DispatchStrategy.SYNC_EMI, true, true, 10L);
	}
	
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		dispatch(x -> x.registerGuiHandlers(registration), DispatchStrategy.SYNC_EMI, true, true, 10L);
	}
	
	public void registerAdvanced(IAdvancedRegistration registration) {
		dispatch(x -> x.registerAdvanced(registration), DispatchStrategy.SYNC_EMI, true, true, 10L);
	}
	
	public void registerRuntime(IRuntimeRegistration registration) {
		dispatch(x -> x.registerRuntime(registration), DispatchStrategy.SYNC_EMI, true, true, 10L);
	}
	
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		dispatch(x -> x.onRuntimeAvailable(jeiRuntime), DispatchStrategy.SYNC_EMI, true, true, 100L);
	}
	
	public void onRuntimeUnavailable() {
		dispatch(IModPlugin::onRuntimeUnavailable, DispatchStrategy.SYNC_EMI, true, true, 10L);
	}
	
}
