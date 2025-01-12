package dev.nolij.toomanyrecipeviewers;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import dev.emi.emi.jemi.JemiUtil;
import dev.nolij.libnolij.collect.InverseSet;
import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.toomanyrecipeviewers.impl.api.registration.RuntimeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIKeyMappings;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.config.JEIConfigManager;
import dev.nolij.toomanyrecipeviewers.impl.common.config.JEIClientConfigs;
import dev.nolij.toomanyrecipeviewers.impl.common.network.ConnectionToServer;
import dev.nolij.toomanyrecipeviewers.impl.library.config.ModIDFormatConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.JeiFeatures;
import mezz.jei.common.config.ClientToggleState;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.StackHelper;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.gui.startup.JeiGuiStarter;
import mezz.jei.library.color.ColorHelper;
import mezz.jei.library.config.ColorNameConfig;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.config.RecipeCategorySortingConfig;
import mezz.jei.library.focus.FocusFactory;
import mezz.jei.library.gui.helpers.GuiHelper;
import mezz.jei.library.helpers.CodecHelper;
import mezz.jei.library.helpers.ModIdHelper;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.IngredientVisibility;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.*;
import mezz.jei.library.plugins.debug.JeiDebugPlugin;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.anvil.SmithingRecipeCategory;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.recipes.RecipeManager;
import mezz.jei.library.recipes.RecipeManagerInternal;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.library.transfer.RecipeTransferHandlerHelper;
import mezz.jei.neoforge.platform.FluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.*;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class TooManyRecipeViewersMod {
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Refraction REFRACTION = new Refraction(MethodHandles.lookup());
	
	@SuppressWarnings("SameParameterValue")
	private static <T> List<Class<? extends T>> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
		final Type annotationType = Type.getType(annotationClass);
		
		final List<Class<? extends T>> result = new ArrayList<>();
		for (final ModFileScanData scanData : ModList.get().getAllScanData()) {
			for (ModFileScanData.AnnotationData annotation : scanData.getAnnotations()) {
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
	
	private static final List<IModPlugin> jeiPlugins;
	private static final VanillaPlugin jeiVanillaPlugin;
	
	static {
		final List<Class<? extends IModPlugin>> pluginClasses = getInstances(JeiPlugin.class, IModPlugin.class);
		
		pluginClasses.remove(JeiDebugPlugin.class);
		
		final ArrayList<IModPlugin> plugins = new ArrayList<>();
		VanillaPlugin vanillaPlugin = null;
		for (final Class<? extends IModPlugin> pluginClass : pluginClasses) {
			final IModPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
			final ResourceLocation id = plugin.getPluginUid();
			
			if (modsWithEMIPlugins.contains(id.getNamespace()))
				continue;
			
			plugins.add(plugin);
			if (plugin instanceof VanillaPlugin _vanillaPlugin) {
				if (vanillaPlugin != null)
					throw new AssertionError("Vanilla plugin already exists!");
				
				vanillaPlugin = _vanillaPlugin;
			}
		}
		
		jeiPlugins = plugins;
		jeiVanillaPlugin = vanillaPlugin;
	}
	
	private static final JEIConfigManager jeiConfigManager = new JEIConfigManager();
	
	public TooManyRecipeViewersMod(IEventBus modEventBus) {
		Internal.setServerConnection(new ConnectionToServer());
		Internal.setJeiClientConfigs(new JEIClientConfigs());
		
		LOGGER.info("Loading JEI Plugins: [{}]", jeiPlugins.stream().map(x -> x.getPluginUid().toString()).collect(Collectors.joining(", ")));
		jeiPlugins.forEach(x -> x.onConfigManagerAvailable(jeiConfigManager));
		
		NeoForge.EVENT_BUS.addListener(RecipesUpdatedEvent.class, event -> register());
//		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> onRuntimeUnavailable());
		modEventBus.addListener(this::onRegisterClientReloadListeners);
	}
	
	private static class JEIRuntimeStorage {
		private volatile SubtypeManager subtypeManager = null;
		private volatile ColorHelper colorHelper = null;
		private volatile IIngredientManager ingredientManager = null;
		private volatile ImmutableSetMultimap<String, String> modAliases = null;
		private volatile IClientToggleState clientToggleState = null;
		private volatile EditModeConfig editModeConfig = null;
		private volatile JeiHelpers jeiHelpers = null;
		private volatile CraftingRecipeCategory craftingCategory = null;
		private volatile SmithingRecipeCategory smithingCategory = null;
		private volatile IRecipeManager recipeManager = null;
		private volatile IRecipeTransferManager recipeTransferManager = null;
		private volatile IScreenHelper screenHelper = null;
		private volatile ResourceManagerReloadListener resourceReloadHandler = null;
		private volatile JEIRuntime jeiRuntime = null;
	}
	
	private volatile JEIRuntimeStorage storage = null;
	
	private void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(Internal.getTextures().getSpriteUploader());
		event.registerReloadListener((ResourceManagerReloadListener) (ResourceManager resourceManager) -> {
			final JEIRuntimeStorage storage = this.storage;
			if (storage != null && storage.resourceReloadHandler != null)
				storage.resourceReloadHandler.onResourceManagerReload(resourceManager);
		});
	}
	
	public void register() {
		synchronized (this) {
			onRuntimeUnavailable();
			
			storage = new JEIRuntimeStorage();
			
			registerSubtypes();
			registerIngredients();
			registerModAliases();
			createJeiHelpers();
			createRecipeManager();
			registerRecipeTransferHandlers();
			registerGuiHandlers();
			
			onRuntimeAvailable();
		}
	}
	
	private void onRuntimeUnavailable() {
		synchronized (this) {
			if (storage != null) {
				storage = null;
				Internal.setRuntime(null);
				
				jeiPlugins.forEach(IModPlugin::onRuntimeUnavailable);
			}
		}
	}
	
	private void registerSubtypes() {
		final SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		jeiPlugins.forEach(x -> x.registerItemSubtypes(subtypeRegistration));
		jeiPlugins.forEach(x -> x.registerFluidSubtypes(subtypeRegistration, new FluidHelper()));
		storage.subtypeManager = new SubtypeManager(subtypeRegistration.getInterpreters());
	}
	
	private void registerIngredients() {
		storage.colorHelper = new ColorHelper(new ColorNameConfig());
		final IngredientManagerBuilder ingredientManagerBuilder = new IngredientManagerBuilder(storage.subtypeManager, storage.colorHelper);
		jeiPlugins.forEach(x -> x.registerIngredients(ingredientManagerBuilder));
		jeiPlugins.forEach(x -> x.registerExtraIngredients(ingredientManagerBuilder));
		jeiPlugins.forEach(x -> x.registerIngredientAliases(ingredientManagerBuilder));
		storage.ingredientManager = ingredientManagerBuilder.build();
	}
	
	private void registerModAliases() {
		final ModInfoRegistration modInfoRegistration = new ModInfoRegistration();
		jeiPlugins.forEach(x -> x.registerModInfo(modInfoRegistration));
		storage.modAliases = modInfoRegistration.getModAliases();
	}
	
	private void createJeiHelpers() {
		final IFocusFactory focusFactory = new FocusFactory(storage.ingredientManager);
		
		final IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		storage.ingredientManager.registerIngredientListener(blacklist);
		
		storage.clientToggleState = new ClientToggleState();
		
		storage.editModeConfig = new EditModeConfig(new EditModeConfig.ISerializer() {
			@Override public void initialize(@NotNull EditModeConfig editModeConfig) {}
			@Override public void save(@NotNull EditModeConfig editModeConfig) {}
			@Override public void load(@NotNull EditModeConfig editModeConfig) {}
		}, storage.ingredientManager);
		
		storage.jeiHelpers = new JeiHelpers(
			new GuiHelper(storage.ingredientManager),
			new StackHelper(storage.subtypeManager),
			new ModIdHelper(new ModIDFormatConfig(), storage.ingredientManager, storage.modAliases),
			focusFactory,
			storage.colorHelper,
			storage.ingredientManager,
			new VanillaRecipeFactory(storage.ingredientManager),
			new CodecHelper(storage.ingredientManager, focusFactory),
			new IngredientVisibility(blacklist, storage.clientToggleState, storage.editModeConfig, storage.ingredientManager)
		);
	}
	
	private void createRecipeManager() {
		final List<IRecipeCategory<?>> recipeCategories = registerRecipeCategories();
		
		final ImmutableListMultimap<RecipeType<?>, ITypedIngredient<?>> recipeCatalysts = registerRecipeCatalysts();
		
		final RecipeManagerInternal recipeManagerInternal = new RecipeManagerInternal(
			recipeCategories,
			recipeCatalysts,
			storage.ingredientManager,
			new RecipeCategorySortingConfig(),
			storage.jeiHelpers.getIngredientVisibility()
		);
		final RecipeManagerPluginHelper recipeManagerPluginHelper = new RecipeManagerPluginHelper(recipeManagerInternal);
		final AdvancedRegistration advancedRegistration = new AdvancedRegistration(storage.jeiHelpers, new JeiFeatures(), recipeManagerPluginHelper);
		jeiPlugins.forEach(x -> x.registerAdvanced(advancedRegistration));
		
		final List<IRecipeManagerPlugin> recipeManagerPlugins = advancedRegistration.getRecipeManagerPlugins();
		final ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators = advancedRegistration.getRecipeCategoryDecorators();
		recipeManagerInternal.addPlugins(recipeManagerPlugins);
		recipeManagerInternal.addDecorators(recipeCategoryDecorators);
		
		RecipeRegistration recipeRegistration = new RecipeRegistration(storage.jeiHelpers, storage.ingredientManager, recipeManagerInternal);
		jeiPlugins.forEach(x -> x.registerRecipes(recipeRegistration));
		recipeManagerInternal.compact();
		
		storage.recipeManager = new RecipeManager(recipeManagerInternal, storage.ingredientManager);
	}
	
	private @NotNull List<IRecipeCategory<?>> registerRecipeCategories() {
		final RecipeCategoryRegistration recipeCategoryRegistration = new RecipeCategoryRegistration(storage.jeiHelpers);
		jeiPlugins.forEach(x -> x.registerCategories(recipeCategoryRegistration));
		
		storage.craftingCategory = jeiVanillaPlugin.getCraftingCategory()
			.orElseThrow(() -> new AssertionError("JEI Vanilla plugin has no crafting category!"));
		storage.smithingCategory = jeiVanillaPlugin.getSmithingCategory()
			.orElseThrow(() -> new AssertionError("JEI Vanilla plugin has no smithing category!"));
		final VanillaCategoryExtensionRegistration vanillaCategoryExtensionRegistration = 
			new VanillaCategoryExtensionRegistration(
				storage.craftingCategory, 
				storage.smithingCategory, 
				storage.jeiHelpers
			);
		jeiPlugins.forEach(x -> x.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration));
		
		return recipeCategoryRegistration.getRecipeCategories();
	}
	
	private @NotNull ImmutableListMultimap<RecipeType<?>, ITypedIngredient<?>> registerRecipeCatalysts() {
		final RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration(storage.ingredientManager, storage.jeiHelpers);
		jeiPlugins.forEach(x -> x.registerRecipeCatalysts(recipeCatalystRegistration));
		return recipeCatalystRegistration.getRecipeCatalysts();
	}
	
	private void registerRecipeTransferHandlers() {
		final IStackHelper stackHelper = storage.jeiHelpers.getStackHelper();
		final IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper(stackHelper, storage.craftingCategory);
		final RecipeTransferRegistration recipeTransferRegistration = new RecipeTransferRegistration(stackHelper, handlerHelper, storage.jeiHelpers, Internal.getServerConnection());
		jeiPlugins.forEach(x -> x.registerRecipeTransferHandlers(recipeTransferRegistration));
		storage.recipeTransferManager = recipeTransferRegistration.createRecipeTransferManager();
	}
	
	private void registerGuiHandlers() {
		final GuiHandlerRegistration guiHandlerRegistration = new GuiHandlerRegistration(storage.jeiHelpers);
		jeiPlugins.forEach(x -> x.registerGuiHandlers(guiHandlerRegistration));
		storage.screenHelper = guiHandlerRegistration.createGuiScreenHelper(storage.ingredientManager);
	}
	
	private void onRuntimeAvailable() {
		final RuntimeRegistration runtimeRegistration = registerRuntime();
		
		final IInternalKeyMappings jeiKeyMappings = new JEIKeyMappings();
		Internal.setKeyMappings(jeiKeyMappings);
		
		final JeiEventHandlers eventHandlers = JeiGuiStarter.start(runtimeRegistration);
		storage.resourceReloadHandler = eventHandlers.resourceReloadHandler();
		
		storage.jeiRuntime = new JEIRuntime(runtimeRegistration, jeiKeyMappings, jeiConfigManager);
		Internal.setRuntime(storage.jeiRuntime);
		jeiPlugins.forEach(x -> x.onRuntimeAvailable(storage.jeiRuntime));
	}
	
	private @NotNull RuntimeRegistration registerRuntime() {
		final RuntimeRegistration runtimeRegistration = new RuntimeRegistration(
			storage.recipeManager,
			storage.jeiHelpers,
			storage.editModeConfig,
			storage.ingredientManager,
			storage.recipeTransferManager,
			storage.screenHelper
		);
		
		jeiPlugins.forEach(x -> x.registerRuntime(runtimeRegistration));
		
		return runtimeRegistration;
	}
	
}
