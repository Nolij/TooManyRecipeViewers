package dev.nolij.toomanyrecipeviewers;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.jemi.JemiRecipeHandler;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiStackSerializer;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.runtime.JemiDragDropHandler;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.nolij.toomanyrecipeviewers.impl.api.recipe.RecipeManager;
import dev.nolij.toomanyrecipeviewers.impl.api.recipe.advanced.RecipeManagerPluginHelper;
import dev.nolij.toomanyrecipeviewers.impl.api.registration.RecipeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.registration.RuntimeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.IngredientManager;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.library.config.ModIDFormatConfig;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.common.Internal;
import mezz.jei.common.JeiFeatures;
import mezz.jei.common.config.ClientToggleState;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.color.ColorHelper;
import mezz.jei.library.config.ColorNameConfig;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.focus.FocusFactory;
import mezz.jei.library.gui.helpers.GuiHelper;
import mezz.jei.library.helpers.CodecHelper;
import mezz.jei.library.helpers.ModIdHelper;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.IngredientVisibility;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.AdvancedRegistration;
import mezz.jei.library.load.registration.GuiHandlerRegistration;
import mezz.jei.library.load.registration.ModInfoRegistration;
import mezz.jei.library.load.registration.RecipeCatalystRegistration;
import mezz.jei.library.load.registration.RecipeCategoryRegistration;
import mezz.jei.library.load.registration.RecipeTransferRegistration;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.load.registration.VanillaCategoryExtensionRegistration;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.library.transfer.RecipeTransferHandlerHelper;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.*;

@EmiEntrypoint
@ApiStatus.Internal
public final class EMIPlugin implements EmiPlugin {
	
	@Override
	public void initialize(EmiInitRegistry registry) {
		onRuntimeUnavailable();
		
		JEIPlugins.resetLoadTimes();
		
		runtime = new TooManyRecipeViewers();
	}
	
	@Override
	public void register(EmiRegistry registry) {
		runtime.emiRegistry = registry;
		
		registerSubtypes();
		registerIngredients();
		registerModAliases();
		createJeiHelpers();
		createRecipeManager();
		registerRecipeTransferHandlers();
		registerGuiHandlers();
		onRuntimeAvailable();
		
		JEIPlugins.logLoadTimes();
		
		runtime.lockRegistration();
		
		registry.addGenericStackProvider((screen, x, y) -> {
			//noinspection removal
			return new EmiStackInteraction(runtime.screenHelper.getClickableIngredientUnderMouse(screen, x, y)
				.map(IClickableIngredient::getTypedIngredient).map(runtime.ingredientManager::getEMIStack).findFirst().orElse(EmiStack.EMPTY), null, false);
		});
		registry.addGenericDragDropHandler(new JemiDragDropHandler());
		registry.removeEmiStacks(emiStack -> {
			try {
				final var jeiIngredient = runtime.ingredientManager.getTypedIngredient(emiStack);
				if (jeiIngredient.isPresent())
					return !runtime.ingredientVisibility.isIngredientVisible(jeiIngredient.get());
			} catch (Throwable ignored) {}
			return false;
		});
	}
	
	private void onRuntimeUnavailable() {
		if (runtime != null) {
			runtime = null;
			Internal.setRuntime(null);
			
			JEIPlugins.onRuntimeUnavailable();
		}
	}
	
	private void registerSubtypes() {
		final var subtypeRegistration = new SubtypeRegistration();
		JEIPlugins.registerItemSubtypes(subtypeRegistration);
		JEIPlugins.registerFluidSubtypes(subtypeRegistration, fluidHelper);
		runtime.subtypeManager = new SubtypeManager(subtypeRegistration.getInterpreters());
		runtime.stackHelper = new StackHelper(runtime.subtypeManager);
	}
	
	private static final EditModeConfig.ISerializer VOID_SERIALIZER = new EditModeConfig.ISerializer() {
		@Override
		public void initialize(EditModeConfig editModeConfig) {}
		
		@Override
		public void save(EditModeConfig editModeConfig) {}
		
		@Override
		public void load(EditModeConfig editModeConfig) {}
	};
	
	private static final Path BLACKLIST_PATH = FMLPaths.CONFIGDIR.get().resolve("jei").resolve("blacklist.json");
	
	private void registerIngredients() {
		runtime.colorHelper = new ColorHelper(new ColorNameConfig());
		runtime.ingredientManager = new IngredientManager(runtime);
		JEIPlugins.registerIngredients(runtime.ingredientManager);
		JEIPlugins.registerExtraIngredients(runtime.ingredientManager);
		JEIPlugins.registerIngredientAliases(runtime.ingredientManager);
		
		runtime.guiHelper = new GuiHelper(runtime.ingredientManager);
		runtime.focusFactory = new FocusFactory(runtime.ingredientManager);
		runtime.codecHelper = new CodecHelper(runtime.ingredientManager, runtime.focusFactory);
		runtime.vanillaRecipeFactory = new VanillaRecipeFactory(runtime.ingredientManager);
		
		runtime.blacklist = new IngredientBlacklistInternal();
		runtime.ingredientManager.registerIngredientListener(runtime.blacklist);
		runtime.clientToggleState = new ClientToggleState();
		final EditModeConfig.ISerializer serializer;
		if (Files.exists(BLACKLIST_PATH))
			serializer = new EditModeConfig.FileSerializer(BLACKLIST_PATH, Objects.requireNonNull(Minecraft.getInstance().level).registryAccess(), runtime.codecHelper);
		else
			serializer = VOID_SERIALIZER;
		runtime.editModeConfig = new EditModeConfig(serializer, runtime.ingredientManager);
		runtime.ingredientVisibility = new IngredientVisibility(runtime.blacklist, runtime.clientToggleState, runtime.editModeConfig, runtime.ingredientManager);
		
		// TODO: use init registry instead?
		//noinspection deprecation
		runtime.emiRegistry.addIngredientSerializer(JemiStack.class, new JemiStackSerializer(runtime.ingredientManager));
	}
	
	private void registerModAliases() {
		final var modInfoRegistration = new ModInfoRegistration();
		JEIPlugins.registerModInfo(modInfoRegistration);
		runtime.modAliases = modInfoRegistration.getModAliases();
		runtime.modIdHelper = new ModIdHelper(new ModIDFormatConfig(), runtime.ingredientManager, runtime.modAliases);
	}
	
	private void createJeiHelpers() {
		runtime.jeiHelpers = new JeiHelpers(
			runtime.guiHelper,
			runtime.stackHelper,
			runtime.modIdHelper,
			runtime.focusFactory,
			runtime.colorHelper,
			runtime.ingredientManager,
			runtime.vanillaRecipeFactory,
			runtime.codecHelper,
			runtime.ingredientVisibility
		);
	}
	
	private void createRecipeManager() {
		registerRecipeCategories();
		registerRecipeCatalysts();
		
		runtime.recipeManager = new RecipeManager(runtime);
		final var recipeManagerPluginHelper = new RecipeManagerPluginHelper(runtime.recipeManager);
		final var advancedRegistration = new AdvancedRegistration(runtime.jeiHelpers, new JeiFeatures(), recipeManagerPluginHelper);
		JEIPlugins.registerAdvanced(advancedRegistration);
		
		runtime.recipeManager.addPlugins(advancedRegistration.getRecipeManagerPlugins());
		runtime.recipeManager.addDecorators(advancedRegistration.getRecipeCategoryDecorators());
		
		final var recipeRegistration = new RecipeRegistration(runtime.jeiHelpers, runtime.ingredientManager, runtime.recipeManager);
		JEIPlugins.registerRecipes(recipeRegistration);
	}
	
	private void registerRecipeCategories() {
		final var recipeCategoryRegistration = new RecipeCategoryRegistration(runtime.jeiHelpers);
		JEIPlugins.registerCategories(recipeCategoryRegistration);
		
		runtime.craftingCategory = JEIPlugins.vanillaPlugin.getCraftingCategory()
			.orElseThrow(() -> new AssertionError("JEI Vanilla plugin has no crafting category!"));
		runtime.smithingCategory = JEIPlugins.vanillaPlugin.getSmithingCategory()
			.orElseThrow(() -> new AssertionError("JEI Vanilla plugin has no smithing category!"));
		final var vanillaCategoryExtensionRegistration =
			new VanillaCategoryExtensionRegistration(
				runtime.craftingCategory,
				runtime.smithingCategory,
				runtime.jeiHelpers
			);
		JEIPlugins.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration);
		
		runtime.recipeCategories = recipeCategoryRegistration.getRecipeCategories();
	}
	
	private void registerRecipeCatalysts() {
		final var recipeCatalystRegistration = new RecipeCatalystRegistration(runtime.ingredientManager, runtime.jeiHelpers);
		JEIPlugins.registerRecipeCatalysts(recipeCatalystRegistration);
		runtime.recipeCatalysts = recipeCatalystRegistration.getRecipeCatalysts();
	}
	
	private void registerRecipeTransferHandlers() {
		final var stackHelper = runtime.stackHelper;
		final var handlerHelper = new RecipeTransferHandlerHelper(stackHelper, runtime.craftingCategory);
		final var recipeTransferRegistration = new RecipeTransferRegistration(stackHelper, handlerHelper, runtime.jeiHelpers, Internal.getServerConnection());
		JEIPlugins.registerRecipeTransferHandlers(recipeTransferRegistration);
		runtime.recipeTransferManager = recipeTransferRegistration.createRecipeTransferManager();
		
		EmiRecipeFiller.extraHandlers = (handler, recipe) -> {
			final var jeiCategory = runtime.recipeManager.category(recipe.getCategory()).getJEICategory();
			if (jeiCategory != null) {
				return runtime.recipeTransferManager
					.getRecipeTransferHandler(handler, jeiCategory)
					.map(JemiRecipeHandler::new)
					.orElse(null);
			}
			return null;
		};
	}
	
	private void registerGuiHandlers() {
		final var guiHandlerRegistration = new GuiHandlerRegistration(runtime.jeiHelpers);
		JEIPlugins.registerGuiHandlers(guiHandlerRegistration);
		runtime.screenHelper = guiHandlerRegistration.createGuiScreenHelper(runtime.ingredientManager);
		
		runtime.emiRegistry.addGenericExclusionArea((screen, consumer) -> {
			final var exclusions = runtime.screenHelper.getGuiExclusionAreas(screen).filter(Objects::nonNull).toList();
			for (final var exclusion : exclusions) {
				consumer.accept(new Bounds(exclusion.getX(), exclusion.getY(), exclusion.getWidth(), exclusion.getHeight()));
			}
		});
	}
	
	private void onRuntimeAvailable() {
		final var runtimeRegistration = registerRuntime();
		
		runtime.jeiRuntime = new JEIRuntime(runtimeRegistration, jeiKeyMappings, jeiConfigManager);
		Internal.setRuntime(runtime.jeiRuntime);
		JEIPlugins.onRuntimeAvailable(runtime.jeiRuntime);
	}
	
	private @NotNull RuntimeRegistration registerRuntime() {
		final var runtimeRegistration = new RuntimeRegistration(runtime);
		
		JEIPlugins.registerRuntime(runtimeRegistration);
		
		return runtimeRegistration;
	}
	
}
