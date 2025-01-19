package dev.nolij.toomanyrecipeviewers;

import com.google.common.collect.ImmutableListMultimap;
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
import dev.nolij.toomanyrecipeviewers.impl.api.registration.RecipeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.registration.RuntimeRegistration;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.library.config.ModIDFormatConfig;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.common.Internal;
import mezz.jei.common.JeiFeatures;
import mezz.jei.common.config.ClientToggleState;
import mezz.jei.common.util.StackHelper;
import mezz.jei.gui.startup.JeiGuiStarter;
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

import java.util.List;
import java.util.Objects;

import static dev.nolij.toomanyrecipeviewers.JEIRuntimeStorage.*;

@EmiEntrypoint
@ApiStatus.Internal
public final class EMIPlugin implements EmiPlugin {
	
	@Override
	public void register(EmiRegistry registry) {
		registry.addGenericDragDropHandler(new JemiDragDropHandler());
		
		onRuntimeUnavailable();
		
		storage = new JEIRuntimeStorage();
		
		registerSubtypes();
		registerIngredients(registry);
		registerModAliases();
		createJeiHelpers();
		createRecipeManager(registry);
		registerRecipeTransferHandlers();
		registerGuiHandlers(registry);
		
		registry.addGenericStackProvider((screen, x, y) -> {
			//noinspection removal
			return new EmiStackInteraction(storage.screenHelper.getClickableIngredientUnderMouse(screen, x, y)
				.map(IClickableIngredient::getTypedIngredient).map(JemiUtil::getStack).findFirst().orElse(EmiStack.EMPTY), null, false);
		});
		
		onRuntimeAvailable();
	}
	
	private void onRuntimeUnavailable() {
		if (storage != null) {
			storage = null;
			Internal.setRuntime(null);
			
			JEIPlugins.onRuntimeUnavailable();
		}
	}
	
	private void registerSubtypes() {
		final var subtypeRegistration = new SubtypeRegistration();
		JEIPlugins.registerItemSubtypes(subtypeRegistration);
		JEIPlugins.registerFluidSubtypes(subtypeRegistration, fluidHelper);
		storage.subtypeManager = new SubtypeManager(subtypeRegistration.getInterpreters());
		storage.stackHelper = new StackHelper(storage.subtypeManager);
	}
	
	private boolean hasSubtype(IIngredientTypeWithSubtypes<?, ?> type, Object ingredient) {
		@SuppressWarnings("unchecked")
		final var castedType = (IIngredientTypeWithSubtypes<Object, Object>) type;
		return storage.subtypeManager.hasSubtypes(castedType, ingredient);
	}
	
	private void registerIngredients(EmiRegistry registry) {
		storage.colorHelper = new ColorHelper(new ColorNameConfig());
		final var ingredientManagerBuilder = new IngredientManagerBuilder(storage.subtypeManager, storage.colorHelper);
		JEIPlugins.registerIngredients(ingredientManagerBuilder);
		JEIPlugins.registerExtraIngredients(ingredientManagerBuilder);
		JEIPlugins.registerIngredientAliases(ingredientManagerBuilder);
		storage.ingredientManager = ingredientManagerBuilder.build();
		
		storage.guiHelper = new GuiHelper(storage.ingredientManager);
		storage.focusFactory = new FocusFactory(storage.ingredientManager);
		storage.codecHelper = new CodecHelper(storage.ingredientManager, storage.focusFactory);
		storage.vanillaRecipeFactory = new VanillaRecipeFactory(storage.ingredientManager);
		
		storage.blacklist = new IngredientBlacklistInternal();
		storage.ingredientManager.registerIngredientListener(storage.blacklist);
		storage.clientToggleState = new ClientToggleState();
		storage.editModeConfig = new EditModeConfig(new EditModeConfig.ISerializer() {
			@Override public void initialize(@NotNull EditModeConfig editModeConfig) {}
			@Override public void save(@NotNull EditModeConfig editModeConfig) {}
			@Override public void load(@NotNull EditModeConfig editModeConfig) {}
		}, storage.ingredientManager);
		// TODO: use
		storage.ingredientVisibility = new IngredientVisibility(storage.blacklist, storage.clientToggleState, storage.editModeConfig, storage.ingredientManager);
		
		// TODO: use init registry instead?
		//noinspection deprecation
		registry.addIngredientSerializer(JemiStack.class, new JemiStackSerializer(storage.ingredientManager));
		
		for (final var ingredientType : storage.ingredientManager.getRegisteredIngredientTypes()) {
			if (ingredientType == VanillaTypes.ITEM_STACK ||
				ingredientType == fluidHelper.getFluidIngredientType())
				continue;
			
			for (final var ingredient : storage.ingredientManager.getAllIngredients(ingredientType)) {
				final var stack = JemiUtil.getStack(ingredientType, ingredient);
				if (!stack.isEmpty()) {
					registry.addEmiStack(stack);
				}
			}
		}
		
		registerItemStackDefaultComparison(registry);
		registerFluidDefaultComparison(registry);
		registerOtherJeiIngredientTypeComparisons(registry);
	}
	
	private void registerItemStackDefaultComparison(EmiRegistry registry) {
		for (final var item : EmiPort.getItemRegistry()) {
			if (hasSubtype(VanillaTypes.ITEM_STACK, item.getDefaultInstance())) {
				//noinspection removal
				registry.setDefaultComparison(item, Comparison.compareData(stack ->
					storage.subtypeManager.getSubtypeInfo(stack.getItemStack(), UidContext.Recipe)));
			}
		}
	}
	
	private void registerFluidDefaultComparison(EmiRegistry registry) {
		for (final var fluid : EmiPort.getFluidRegistry()) {
			//noinspection unchecked
			final var type = (IIngredientTypeWithSubtypes<Object, Object>) JemiUtil.getFluidType();
			//noinspection deprecation
			if (hasSubtype(type, fluidHelper.create(fluid.builtInRegistryHolder(), 1000))) {
				registry.setDefaultComparison(fluid, Comparison.compareData(stack -> {
					final var typed = JemiUtil.getTyped(stack).orElse(null);
					if (typed != null) {
						return storage.subtypeManager.getSubtypeInfo(type, typed.getIngredient(), UidContext.Recipe);
					}
					return null;
				}));
			}
		}
	}
	
	private void registerOtherJeiIngredientTypeComparisons(EmiRegistry registry) {
		final var jeiIngredientTypes = Lists.newArrayList(storage.ingredientManager.getRegisteredIngredientTypes());
		for (final var _jeiIngredientType : jeiIngredientTypes) {
			if (_jeiIngredientType == VanillaTypes.ITEM_STACK || _jeiIngredientType == JemiUtil.getFluidType()) {
				continue;
			}
			//noinspection rawtypes
			if (_jeiIngredientType instanceof final IIngredientTypeWithSubtypes jeiIngredientType) {
				final var jeiIngredients = Lists.newArrayList(storage.ingredientManager.getAllIngredients(_jeiIngredientType));
				for (final var jeiIngredient : jeiIngredients) {
					try {
						if (hasSubtype(jeiIngredientType, jeiIngredient)) {
							//noinspection unchecked
							registry.setDefaultComparison(jeiIngredientType.getBase(jeiIngredient), Comparison.compareData(stack -> {
								if (stack instanceof final JemiStack<?> jemi) {
									//noinspection unchecked
									return storage.subtypeManager.getSubtypeInfo(jeiIngredientType, jemi.ingredient, UidContext.Recipe);
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
		storage.modAliases = modInfoRegistration.getModAliases();
		storage.modIdHelper = new ModIdHelper(new ModIDFormatConfig(), storage.ingredientManager, storage.modAliases);
	}
	
	private void createJeiHelpers() {
		storage.jeiHelpers = new JeiHelpers(
			storage.guiHelper,
			storage.stackHelper,
			storage.modIdHelper,
			storage.focusFactory,
			storage.colorHelper,
			storage.ingredientManager,
			storage.vanillaRecipeFactory,
			storage.codecHelper,
			storage.ingredientVisibility
		);
	}
	
	private void createRecipeManager(EmiRegistry registry) {
		final var recipeCategories = registerRecipeCategories();
		
		final var recipeCatalysts = registerRecipeCatalysts();
		
		storage.recipeManager = new RecipeManager(
			registry,
			recipeCategories,
			recipeCatalysts,
			storage.ingredientManager,
			storage.jeiHelpers.getIngredientVisibility()
		);
		final var recipeManagerPluginHelper = new RecipeManagerPluginHelper(storage.recipeManager);
		final var advancedRegistration = new AdvancedRegistration(storage.jeiHelpers, new JeiFeatures(), recipeManagerPluginHelper);
		JEIPlugins.registerAdvanced(advancedRegistration);
		
		storage.recipeManager.addPlugins(advancedRegistration.getRecipeManagerPlugins());
		storage.recipeManager.addDecorators(advancedRegistration.getRecipeCategoryDecorators());
		
		final var recipeRegistration = new RecipeRegistration(storage.jeiHelpers, storage.ingredientManager, storage.recipeManager);
		JEIPlugins.registerRecipes(recipeRegistration);
		
		storage.recipeManager.compact();
	}
	
	private @NotNull List<IRecipeCategory<?>> registerRecipeCategories() {
		final var recipeCategoryRegistration = new RecipeCategoryRegistration(storage.jeiHelpers);
		JEIPlugins.registerCategories(recipeCategoryRegistration);
		
		storage.craftingCategory = JEIPlugins.vanillaPlugin.getCraftingCategory()
			.orElseThrow(() -> new AssertionError("JEI Vanilla plugin has no crafting category!"));
		storage.smithingCategory = JEIPlugins.vanillaPlugin.getSmithingCategory()
			.orElseThrow(() -> new AssertionError("JEI Vanilla plugin has no smithing category!"));
		final var vanillaCategoryExtensionRegistration =
			new VanillaCategoryExtensionRegistration(
				storage.craftingCategory,
				storage.smithingCategory,
				storage.jeiHelpers
			);
		JEIPlugins.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration);
		
		return recipeCategoryRegistration.getRecipeCategories();
	}
	
	private @NotNull ImmutableListMultimap<RecipeType<?>, ITypedIngredient<?>> registerRecipeCatalysts() {
		final var recipeCatalystRegistration = new RecipeCatalystRegistration(storage.ingredientManager, storage.jeiHelpers);
		JEIPlugins.registerRecipeCatalysts(recipeCatalystRegistration);
		return recipeCatalystRegistration.getRecipeCatalysts();
	}
	
	private void registerRecipeTransferHandlers() {
		final var stackHelper = storage.stackHelper;
		final var handlerHelper = new RecipeTransferHandlerHelper(stackHelper, storage.craftingCategory);
		final var recipeTransferRegistration = new RecipeTransferRegistration(stackHelper, handlerHelper, storage.jeiHelpers, Internal.getServerConnection());
		JEIPlugins.registerRecipeTransferHandlers(recipeTransferRegistration);
		storage.recipeTransferManager = recipeTransferRegistration.createRecipeTransferManager();
		
		EmiRecipeFiller.extraHandlers = (handler, recipe) -> {
			final var category = storage.recipeManager.getRecipeType(recipe.getCategory());
			if (category != null) {
				return storage.recipeTransferManager.getRecipeTransferHandler(handler, category).map(JemiRecipeHandler::new).orElse(null);
			}
			return null;
		};
	}
	
	private void registerGuiHandlers(EmiRegistry registry) {
		final var guiHandlerRegistration = new GuiHandlerRegistration(storage.jeiHelpers);
		JEIPlugins.registerGuiHandlers(guiHandlerRegistration);
		storage.screenHelper = guiHandlerRegistration.createGuiScreenHelper(storage.ingredientManager);
		
		registry.addGenericExclusionArea((screen, consumer) -> {
			final var exclusions = storage.screenHelper.getGuiExclusionAreas(screen).filter(Objects::nonNull).toList();
			for (final var exclusion : exclusions) {
				consumer.accept(new Bounds(exclusion.getX(), exclusion.getY(), exclusion.getWidth(), exclusion.getHeight()));
			}
		});
	}
	
	private void onRuntimeAvailable() {
		final var runtimeRegistration = registerRuntime();
		
		final var eventHandlers = JeiGuiStarter.start(runtimeRegistration);
		storage.resourceReloadHandler = eventHandlers.resourceReloadHandler();
		
		storage.jeiRuntime = new JEIRuntime(runtimeRegistration, jeiKeyMappings, jeiConfigManager);
		Internal.setRuntime(storage.jeiRuntime);
		JEIPlugins.onRuntimeAvailable(storage.jeiRuntime);
	}
	
	private @NotNull RuntimeRegistration registerRuntime() {
		final var runtimeRegistration = new RuntimeRegistration(
			storage.recipeManager,
			storage.jeiHelpers,
			storage.editModeConfig,
			storage.ingredientManager,
			storage.recipeTransferManager,
			storage.screenHelper
		);
		
		JEIPlugins.registerRuntime(runtimeRegistration);
		
		return runtimeRegistration;
	}
	
}
