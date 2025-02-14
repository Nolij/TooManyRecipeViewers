package dev.nolij.toomanyrecipeviewers;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.jemi.JemiRecipeHandler;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiStackSerializer;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.runtime.JemiDragDropHandler;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.nolij.toomanyrecipeviewers.impl.api.recipe.RecipeManager;
import dev.nolij.toomanyrecipeviewers.impl.api.recipe.advanced.RecipeManagerPluginHelper;
import dev.nolij.toomanyrecipeviewers.impl.api.registration.IngredientAliasRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.registration.RecipeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.registration.RuntimeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.library.config.ModIDFormatConfig;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
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
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.load.registration.ModInfoRegistration;
import mezz.jei.library.load.registration.RecipeCatalystRegistration;
import mezz.jei.library.load.registration.RecipeCategoryRegistration;
import mezz.jei.library.load.registration.RecipeTransferRegistration;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.load.registration.VanillaCategoryExtensionRegistration;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.library.transfer.RecipeTransferHandlerHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.*;

@EmiEntrypoint
@ApiStatus.Internal
public final class EMIPlugin implements EmiPlugin {
	
	@Override
	public void register(EmiRegistry registry) {
		registry.addGenericDragDropHandler(new JemiDragDropHandler());
		
		onRuntimeUnavailable();
		
		JEIPlugins.resetLoadTimes();
		
		runtime = new TooManyRecipeViewers();
		runtime.emiRegistry = registry;
		
		registerSubtypes();
		registerIngredients();
		registerModAliases();
		createJeiHelpers();
		createRecipeManager();
		registerRecipeTransferHandlers();
		registerGuiHandlers();
		
		registry.addGenericStackProvider((screen, x, y) -> {
			//noinspection removal
			return new EmiStackInteraction(runtime.screenHelper.getClickableIngredientUnderMouse(screen, x, y)
				.map(IClickableIngredient::getTypedIngredient).map(JemiUtil::getStack).findFirst().orElse(EmiStack.EMPTY), null, false);
		});
		
		onRuntimeAvailable();
		
		runtime.recipeManager.lock();
		
		JEIPlugins.logLoadTimes();
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
	
	private boolean hasSubtype(IIngredientTypeWithSubtypes<?, ?> type, Object ingredient) {
		@SuppressWarnings("unchecked")
		final var castedType = (IIngredientTypeWithSubtypes<Object, Object>) type;
		return runtime.subtypeManager.hasSubtypes(castedType, ingredient);
	}
	
	private void registerIngredients() {
		runtime.colorHelper = new ColorHelper(new ColorNameConfig());
		final var ingredientManagerBuilder = new IngredientManagerBuilder(runtime.subtypeManager, runtime.colorHelper);
		JEIPlugins.registerIngredients(ingredientManagerBuilder);
		JEIPlugins.registerExtraIngredients(ingredientManagerBuilder);
		runtime.ingredientAliasRegistration = new IngredientAliasRegistration();
		JEIPlugins.registerIngredientAliases(runtime.ingredientAliasRegistration);
		runtime.ingredientManager = ingredientManagerBuilder.build();
		
		runtime.guiHelper = new GuiHelper(runtime.ingredientManager);
		runtime.focusFactory = new FocusFactory(runtime.ingredientManager);
		runtime.codecHelper = new CodecHelper(runtime.ingredientManager, runtime.focusFactory);
		runtime.vanillaRecipeFactory = new VanillaRecipeFactory(runtime.ingredientManager);
		
		runtime.blacklist = new IngredientBlacklistInternal();
		runtime.ingredientManager.registerIngredientListener(runtime.blacklist);
		runtime.clientToggleState = new ClientToggleState();
		runtime.editModeConfig = new EditModeConfig(new EditModeConfig.ISerializer() {
			@Override public void initialize(@NotNull EditModeConfig editModeConfig) {}
			@Override public void save(@NotNull EditModeConfig editModeConfig) {}
			@Override public void load(@NotNull EditModeConfig editModeConfig) {}
		}, runtime.ingredientManager);
		// TODO: use?
		runtime.ingredientVisibility = new IngredientVisibility(runtime.blacklist, runtime.clientToggleState, runtime.editModeConfig, runtime.ingredientManager);
		
		// TODO: use init registry instead?
		//noinspection deprecation
		runtime.emiRegistry.addIngredientSerializer(JemiStack.class, new JemiStackSerializer(runtime.ingredientManager));
		
		for (final var ingredientType : runtime.ingredientManager.getRegisteredIngredientTypes()) {
			if (ingredientType == VanillaTypes.ITEM_STACK ||
				ingredientType == fluidHelper.getFluidIngredientType())
				continue;
			
			for (final var ingredient : runtime.ingredientManager.getAllIngredients(ingredientType)) {
				final var stack = JemiUtil.getStack(ingredientType, ingredient);
				if (!stack.isEmpty()) {
					runtime.emiRegistry.addEmiStack(stack);
				}
			}
		}
		
		registerItemStackDefaultComparison();
		registerFluidDefaultComparison();
		registerOtherJeiIngredientTypeComparisons();
	}
	
	private void registerItemStackDefaultComparison() {
		for (final var item : EmiPort.getItemRegistry()) {
			if (hasSubtype(VanillaTypes.ITEM_STACK, item.getDefaultInstance())) {
				//noinspection removal
				runtime.emiRegistry.setDefaultComparison(item, Comparison.compareData(stack ->
					runtime.subtypeManager.getSubtypeInfo(stack.getItemStack(), UidContext.Recipe)));
			}
		}
	}
	
	private void registerFluidDefaultComparison() {
		for (final var fluid : EmiPort.getFluidRegistry()) {
			//noinspection unchecked
			final var type = (IIngredientTypeWithSubtypes<Object, Object>) JemiUtil.getFluidType();
			//noinspection deprecation
			if (hasSubtype(type, fluidHelper.create(fluid.builtInRegistryHolder(), 1000))) {
				runtime.emiRegistry.setDefaultComparison(fluid, Comparison.compareData(stack -> {
					final var typed = JemiUtil.getTyped(stack).orElse(null);
					if (typed != null) {
						return runtime.subtypeManager.getSubtypeInfo(type, typed.getIngredient(), UidContext.Recipe);
					}
					return null;
				}));
			}
		}
	}
	
	private void registerOtherJeiIngredientTypeComparisons() {
		final var jeiIngredientTypes = Lists.newArrayList(runtime.ingredientManager.getRegisteredIngredientTypes());
		for (final var _jeiIngredientType : jeiIngredientTypes) {
			if (_jeiIngredientType == VanillaTypes.ITEM_STACK || _jeiIngredientType == JemiUtil.getFluidType()) {
				continue;
			}
			//noinspection rawtypes
			if (_jeiIngredientType instanceof final IIngredientTypeWithSubtypes jeiIngredientType) {
				final var jeiIngredients = Lists.newArrayList(runtime.ingredientManager.getAllIngredients(_jeiIngredientType));
				for (final var jeiIngredient : jeiIngredients) {
					try {
						if (hasSubtype(jeiIngredientType, jeiIngredient)) {
							//noinspection unchecked
							runtime.emiRegistry.setDefaultComparison(jeiIngredientType.getBase(jeiIngredient), Comparison.compareData(stack -> {
								if (stack instanceof final JemiStack<?> jemi) {
									//noinspection unchecked
									return runtime.subtypeManager.getSubtypeInfo(jeiIngredientType, jemi.ingredient, UidContext.Recipe);
								}
								return null;
							}));
						}
					} catch (Throwable t) {
						EmiReloadLog.warn("Exception adding default comparison for JEI ingredient");
						EmiReloadLog.error(t);
					}
				}
			}
		}
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
				return runtime.recipeTransferManager.getRecipeTransferHandler(handler, jeiCategory).map(JemiRecipeHandler::new).orElse(null);
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
