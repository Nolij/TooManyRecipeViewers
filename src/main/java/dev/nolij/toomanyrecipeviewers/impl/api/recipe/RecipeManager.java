package dev.nolij.toomanyrecipeviewers.impl.api.recipe;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiCategory;
import dev.emi.emi.jemi.JemiRecipe;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.registry.EmiRecipes;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.util.ResourceLocationHolderComparator;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeCatalystLookup;
import mezz.jei.api.recipe.IRecipeCategoriesLookup;
import mezz.jei.api.recipe.IRecipeLookup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.ingredients.CycleTimer;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.gui.recipes.layout.RecipeLayoutDrawableErrored;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.recipes.InternalRecipeManagerPlugin;
import mezz.jei.library.recipes.PluginManager;
import mezz.jei.library.recipes.RecipeCatalystBuilder;
import mezz.jei.library.recipes.collect.RecipeMap;
import mezz.jei.library.recipes.collect.RecipeTypeData;
import mezz.jei.library.recipes.collect.RecipeTypeDataMap;
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.library.util.RecipeErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class RecipeManager implements IRecipeManager {
	
	public static final Map<RecipeType<?>, EmiRecipeCategory> vanillaJEITypeEMICategoryMap =
		ImmutableMap.<RecipeType<?>, EmiRecipeCategory>builder()
			.put(RecipeTypes.CRAFTING, VanillaEmiRecipeCategories.CRAFTING)
			.put(RecipeTypes.SMELTING, VanillaEmiRecipeCategories.SMELTING)
			.put(RecipeTypes.BLASTING, VanillaEmiRecipeCategories.BLASTING)
			.put(RecipeTypes.SMOKING, VanillaEmiRecipeCategories.SMOKING)
			.put(RecipeTypes.CAMPFIRE_COOKING, VanillaEmiRecipeCategories.CAMPFIRE_COOKING)
			.put(RecipeTypes.STONECUTTING, VanillaEmiRecipeCategories.STONECUTTING)
			.put(RecipeTypes.SMITHING, VanillaEmiRecipeCategories.SMITHING)
			.put(RecipeTypes.ANVIL, VanillaEmiRecipeCategories.ANVIL_REPAIRING)
			.put(RecipeTypes.BREWING, VanillaEmiRecipeCategories.BREWING)
			.put(RecipeTypes.FUELING, VanillaEmiRecipeCategories.FUEL)
			.put(RecipeTypes.COMPOSTING, VanillaEmiRecipeCategories.COMPOSTING)
			.put(RecipeTypes.INFORMATION, VanillaEmiRecipeCategories.INFO)
			.build();
	public static final Map<EmiRecipeCategory, RecipeType<?>> vanillaEMICategoryJEIRecipeTypeMap;
	static {
		final var vanillaEMICategoryJEIRecipeClassMapBuilder = ImmutableMap.<EmiRecipeCategory, RecipeType<?>>builder();
		vanillaJEITypeEMICategoryMap.forEach((k, v) ->
			vanillaEMICategoryJEIRecipeClassMapBuilder.put(v, k));
		vanillaEMICategoryJEIRecipeTypeMap = vanillaEMICategoryJEIRecipeClassMapBuilder.build();
	}
	
	private final TooManyRecipeViewers runtime;
	
	private final EmiRegistry registry;
	private final @Unmodifiable List<IRecipeCategory<?>> jeiRecipeCategories;
	private final IIngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	
	private final RecipeTypeDataMap recipeTypeDataMap;
	private final EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps;
	private final Set<RecipeType<?>> hiddenRecipeTypes = new HashSet<>();
	private final PluginManager pluginManager;
	private ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators;
	
	private volatile boolean locked = false;
	
	@Nullable
	@Unmodifiable
	private List<IRecipeCategory<?>> recipeCategoriesVisibleCache = null;
	
	public RecipeManager(TooManyRecipeViewers runtime) {
		this.runtime = runtime;
		this.registry = runtime.emiRegistry;
		this.jeiRecipeCategories = runtime.recipeCategories;
		this.ingredientManager = runtime.ingredientManager;
		this.ingredientVisibility = runtime.ingredientVisibility;
		
		for (final var jeiCategory : jeiRecipeCategories) {
			final var jeiRecipeType = jeiCategory.getRecipeType();
			
			final var jeiCatalysts = runtime.recipeCatalysts.get(jeiRecipeType);
			final var emiCatalysts = jeiCatalysts.stream().map(JemiUtil::getStack).toList();
			
			final var category = runtime.recipeCategory(jeiCategory);
			final var emiCategory = category.getEMICategory();
			if (emiCategory instanceof JemiCategory)
				registry.addCategory(emiCategory);
			
			for (final var emiCatalyst : emiCatalysts) {
				if (!emiCatalyst.isEmpty()) {
					registry.addWorkstation(emiCategory, emiCatalyst);
				}
			}
		}
		
		this.recipeMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (final var role : RecipeIngredientRole.values()) {
			final var recipeMap = new RecipeMap(ResourceLocationHolderComparator.create(RecipeType::getUid), ingredientManager, role);
			this.recipeMaps.put(role, recipeMap);
		}
		
		final var recipeCatalystBuilder = new RecipeCatalystBuilder(this.recipeMaps.get(RecipeIngredientRole.CATALYST));
		for (final var recipeCategory : jeiRecipeCategories) {
			final var recipeType = recipeCategory.getRecipeType();
			if (runtime.recipeCatalysts.containsKey(recipeType)) {
				final var catalysts = runtime.recipeCatalysts.get(recipeType);
				recipeCatalystBuilder.addCategoryCatalysts(recipeCategory, catalysts);
			}
		}
		final var recipeCategoryCatalystsMap = recipeCatalystBuilder.buildRecipeCategoryCatalysts();
		this.recipeTypeDataMap = new RecipeTypeDataMap(jeiRecipeCategories, recipeCategoryCatalystsMap);
		
		this.pluginManager = new PluginManager(new InternalRecipeManagerPlugin(ingredientManager, recipeTypeDataMap, recipeMaps));
	}
	
	//region IRecipeManager
	@Override
	public <R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType) {
		return new IRecipeLookup<>() {
			private boolean includeHidden = false;
			private IFocusGroup focusGroup = FocusGroup.EMPTY;
			
			@Override
			public IRecipeLookup<R> limitFocus(Collection<? extends IFocus<?>> focuses) {
				this.focusGroup = FocusGroup.create(focuses, ingredientManager);
				return this;
			}
			
			@Override
			public IRecipeLookup<R> includeHidden() {
				this.includeHidden = true;
				return this;
			}
			
			@Override
			public Stream<R> get() {
				return getRecipesStream(recipeType, focusGroup, includeHidden);
			}
		};
	}
	
	@Override
	public IRecipeCategoriesLookup createRecipeCategoryLookup() {
		return new IRecipeCategoriesLookup() {
			private boolean includeHidden = false;
			private Collection<RecipeType<?>> recipeTypes = List.of();
			private IFocusGroup focusGroup = FocusGroup.EMPTY;
			
			@Override
			public IRecipeCategoriesLookup limitTypes(Collection<RecipeType<?>> recipeTypes) {
				ErrorUtil.checkNotNull(recipeTypes, "recipeTypes");
				this.recipeTypes = recipeTypes;
				return this;
			}
			
			@Override
			public IRecipeCategoriesLookup limitFocus(Collection<? extends IFocus<?>> focuses) {
				ErrorUtil.checkNotNull(focuses, "focuses");
				this.focusGroup = FocusGroup.create(focuses, ingredientManager);
				return this;
			}
			
			@Override
			public IRecipeCategoriesLookup includeHidden() {
				this.includeHidden = true;
				return this;
			}
			
			@Override
			public Stream<IRecipeCategory<?>> get() {
				return getRecipeCategoriesForTypes(recipeTypes, focusGroup, includeHidden);
			}
		};
	}
	
	@Override
	public <T> IRecipeCategory<T> getRecipeCategory(RecipeType<T> recipeType) {
		return runtime.recipeCategory(recipeType).getJEICategory();
	}
	
	@Override
	public IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType) {
		return new IRecipeCatalystLookup() {
			private boolean includeHidden;
			
			@Override
			public IRecipeCatalystLookup includeHidden() {
				this.includeHidden = true;
				return this;
			}
			
			@Override
			public Stream<ITypedIngredient<?>> get() {
				return getRecipeCatalystStream(recipeType, includeHidden);
			}
			
			@Override
			public <V> Stream<V> get(IIngredientType<V> ingredientType) {
				return get()
					.map(i -> i.getIngredient(ingredientType))
					.flatMap(Optional::stream);
			}
		};
	}
	
	@Override
	public <T> void addRecipes(RecipeType<T> jeiRecipeType, List<T> jeiRecipes) {
		if (locked)
			throw new IllegalStateException("Tried to add recipes after registry is locked");
		
		if (jeiRecipeType == RecipeTypes.INFORMATION) {
			//noinspection unchecked
			final var jeiInfoRecipes = (List<IJeiIngredientInfoRecipe>) jeiRecipes;
			
			for (final var jeiInfoRecipe : jeiInfoRecipes) {
				final var emiIngredients = jeiInfoRecipe
					.getIngredients()
					.stream()
					.map(JemiUtil::getStack)
					.map(EmiIngredient.class::cast)
					.toList();
				
				final var lines = jeiInfoRecipe
					.getDescription()
					.stream()
					.map(formattedText -> {
						if (formattedText instanceof Component component)
							return component;
						
						var result = Component.literal("");
						
						formattedText.visit((style, string) -> {
							result.append(Component.literal(string).withStyle(style));
							
							return Optional.empty();
						}, Style.EMPTY);
						
						return result;
					})
					.toList();
				
				final var emiInfoRecipe = new EmiInfoRecipe(emiIngredients, lines, null);
				registry.addRecipe(emiInfoRecipe);
				recipeCategoriesVisibleCache = null;
			}
			return;
//		} else if (jeiRecipeType == RecipeTypes.CRAFTING) {
//			LOGGER.info("CRAFTING recipes added from {}", new Exception().getStackTrace()[2].getClassName());
		}
		
		final var emiCategory = runtime.recipeCategory(jeiRecipeType).getEMICategory();
		
		final var recipeTypeData = recipeTypeDataMap.get(jeiRecipeType);
		final var jeiCategory = recipeTypeData.getRecipeCategory();
//		final var jeiCategory = getRecipeCategory(jeiRecipeType);
		final var hiddenRecipes = recipeTypeData.getHiddenRecipes();
		
		final var addedRecipes = new ArrayList<T>(jeiRecipes.size());
		for (final var jeiRecipe : jeiRecipes) {
			if (addRecipe(emiCategory, jeiCategory, jeiRecipe, hiddenRecipes)) {
				addedRecipes.add(jeiRecipe);
			}
		}
		
		if (!addedRecipes.isEmpty()) {
			recipeTypeData.addRecipes(addedRecipes);
			recipeCategoriesVisibleCache = null;
		}
	}
	
	@Override
	public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		final var recipeTypeData = recipeTypeDataMap.get(recipeType);
		final var hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.addAll(recipes);
		recipeCategoriesVisibleCache = null;
	}
	
	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		final var recipeTypeData = recipeTypeDataMap.get(recipeType);
		final var hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.removeAll(recipes);
		recipeCategoriesVisibleCache = null;
	}
	
	@Override
	public void hideRecipeCategory(RecipeType<?> recipeType) {
		hiddenRecipeTypes.add(recipeType);
		recipeCategoriesVisibleCache = null;
	}
	
	@Override
	public void unhideRecipeCategory(RecipeType<?> recipeType) {
		recipeTypeDataMap.validate(recipeType);
		hiddenRecipeTypes.remove(recipeType);
		recipeCategoriesVisibleCache = null;
	}
	
	@Override
	public <T> IRecipeLayoutDrawable<T> createRecipeLayoutDrawableOrShowError(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focusGroup) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");
		
		final var recipeType = recipeCategory.getRecipeType();
		final var decorators = getRecipeCategoryDecorators(recipeType);
		
		final IScalableDrawable recipeBackground;
		final int borderPadding;
		if (recipeCategory.needsRecipeBorder()) {
			recipeBackground = Internal.getTextures().getRecipeBackground();
			borderPadding = 4;
		} else {
			recipeBackground = DrawableBlank.EMPTY;
			borderPadding = 0;
		}
		
		return 
			RecipeLayout.create(
				recipeCategory,
				decorators,
				recipe,
				focusGroup,
				ingredientManager,
				recipeBackground,
				borderPadding
			)
			.orElseGet(() -> new RecipeLayoutDrawableErrored<>(recipeCategory, recipe, recipeBackground, borderPadding));
	}
	
	@Override
	public <T> Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focusGroup) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");
		
		final var recipeType = recipeCategory.getRecipeType();
		final var decorators = getRecipeCategoryDecorators(recipeType);
		
		final IScalableDrawable recipeBackground;
		final int borderPadding;
		if (recipeCategory.needsRecipeBorder()) {
			recipeBackground = Internal.getTextures().getRecipeBackground();
			borderPadding = 4;
		} else {
			recipeBackground = DrawableBlank.EMPTY;
			borderPadding = 0;
		}
		
		return 
			RecipeLayout.create(
				recipeCategory,
				decorators,
				recipe,
				focusGroup,
				ingredientManager,
				recipeBackground,
				borderPadding
			);
	}
	
	@Override
	public <T> Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable(
		IRecipeCategory<T> recipeCategory,
		T recipe,
		IFocusGroup focusGroup,
		IScalableDrawable background,
		int borderSize
	) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");
		ErrorUtil.checkNotNull(background, "background");
		
		final var recipeType = recipeCategory.getRecipeType();
		final var decorators = getRecipeCategoryDecorators(recipeType);
		return RecipeLayout.create(
			recipeCategory,
			decorators,
			recipe,
			focusGroup,
			ingredientManager,
			background,
			borderSize
		);
	}
	
	@Override
	public IRecipeSlotDrawable createRecipeSlotDrawable(RecipeIngredientRole role, List<Optional<ITypedIngredient<?>>> ingredients, Set<Integer> focusedIngredients, int ingredientCycleOffset) {
		final var builder = new RecipeSlotBuilder(ingredientManager, 0, role);
		builder.addOptionalTypedIngredients(ingredients);
		final var cycleTimer = CycleTimer.create(ingredientCycleOffset);
		final var result = builder.build(focusedIngredients, cycleTimer);
		return result.second();
	}
	
	@Override
	public <T> IIngredientSupplier getRecipeIngredients(IRecipeCategory<T> jeiCategory, T recipe) {
		return IngredientSupplierHelper.getIngredientSupplier(recipe, jeiCategory, ingredientManager);
	}
	
	@Override
	public <T> Optional<RecipeType<T>> getRecipeType(ResourceLocation recipeUid, Class<? extends T> recipeClass) {
		return recipeTypeDataMap.getType(recipeUid, recipeClass);
	}
	
	@Override
	public Optional<RecipeType<?>> getRecipeType(ResourceLocation recipeUid) {
		return recipeTypeDataMap.getType(recipeUid);
	}
	//endregion
	
	//region RecipeManagerInternal
	private final Set<EmiRecipe> replacementRecipes = new HashSet<>();
	
	private <T> boolean addRecipe(EmiRecipeCategory emiCategory, IRecipeCategory<T> jeiCategory, T jeiRecipe, Set<T> hiddenRecipes) {
		final var jeiRecipeType = jeiCategory.getRecipeType();
		if (hiddenRecipes.contains(jeiRecipe)) {
			if (LOGGER.isDebugEnabled()) {
				String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(jeiRecipe, jeiCategory, ingredientManager);
				LOGGER.debug("Recipe not added because it is hidden: {}", recipeInfo);
			}
			return false;
		}
		if (!jeiCategory.isHandled(jeiRecipe)) {
			if (LOGGER.isDebugEnabled()) {
				String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(jeiRecipe, jeiCategory, ingredientManager);
				LOGGER.debug("Recipe not added because the recipe category cannot handle it: {}", recipeInfo);
			}
			return false;
		}
		final var ingredientSupplier = IngredientSupplierHelper.getIngredientSupplier(jeiRecipe, jeiCategory, ingredientManager);
		
		try {
			for (final var recipeMap : recipeMaps.values()) {
				recipeMap.addRecipe(jeiRecipeType, jeiRecipe, ingredientSupplier);
			}
			
			final var recipe = runtime.recipe(runtime.recipeCategory(emiCategory), jeiRecipe);
			final var emiRecipe = recipe.getEMIRecipe();
			replacementRecipes.add(emiRecipe);
			registry.addRecipe(emiRecipe);
			if (vanillaJEITypeEMICategoryMap.containsKey(jeiRecipeType)) {
				if (emiRecipe instanceof JemiRecipe<?> jemiRecipe) {
					LOGGER.warn("Recipe replacement for {} will not render properly!", jemiRecipe.originalId);
					registry.removeRecipes(jemiRecipe.originalId);
				} else {
					final var id = emiRecipe.getId();
					final var originalId = recipe.getOriginalID();
					registry.addRecipe(emiRecipe);
					registry.removeRecipes(x ->
						x instanceof EmiRecipe &&
						(Objects.equals(x.getId(), id) || Objects.equals(x.getId(), originalId)) &&
						!replacementRecipes.contains(x));
					return true;
				}
			}
			
			return true;
		} catch (RuntimeException | LinkageError e) {
			final var recipeInfo = RecipeErrorUtil.getInfoFromRecipe(jeiRecipe, jeiCategory, ingredientManager);
			LOGGER.error("Found a broken recipe, failed to addRecipe: {}\n", recipeInfo, e);
			return false;
		}
	}
	
	@Unmodifiable
	@SuppressWarnings("unchecked")
	private <T> List<IRecipeCategoryDecorator<T>> getRecipeCategoryDecorators(RecipeType<T> recipeType) {
		final var decorators = recipeCategoryDecorators.get(recipeType);
		return (List<IRecipeCategoryDecorator<T>>) (Object) decorators;
	}
	
	private boolean isCategoryHidden(IRecipeCategory<?> recipeCategory, IFocusGroup focuses) {
		// hide the category if it has been explicitly hidden
		final var recipeType = recipeCategory.getRecipeType();
		if (hiddenRecipeTypes.contains(recipeType)) {
			return true;
		}
		
		// hide the category if it has catalysts, but they have all been hidden
		if (getRecipeCatalystStream(recipeType, true).findAny().isPresent() &&
			getRecipeCatalystStream(recipeType, false).findAny().isEmpty())
		{
			return true;
		}
		
		// hide the category if it has no recipes, or if the recipes have all been hidden
		final var visibleRecipes = getRecipesStream(recipeType, focuses, false);
		return visibleRecipes.findAny().isEmpty();
	}
	
	private Stream<IRecipeCategory<?>> getRecipeCategoriesForTypes(Collection<RecipeType<?>> recipeTypes, IFocusGroup focuses, boolean includeHidden) {
		final var recipeCategories = recipeTypes.stream()
			.map(this.recipeTypeDataMap::get)
			.<IRecipeCategory<?>>map(RecipeTypeData::getRecipeCategory)
			.toList();
		
		return getRecipeCategoriesCached(recipeCategories, focuses, includeHidden);
	}
	
	private Stream<IRecipeCategory<?>> getRecipeCategoriesCached(Collection<IRecipeCategory<?>> recipeCategories, IFocusGroup focuses, boolean includeHidden) {
		if (recipeCategories.isEmpty() && focuses.isEmpty() && !includeHidden) {
			if (this.recipeCategoriesVisibleCache == null) {
				this.recipeCategoriesVisibleCache = getRecipeCategoriesUncached(recipeCategories, focuses, includeHidden)
					.toList();
			}
			return this.recipeCategoriesVisibleCache.stream();
		}
		
		return getRecipeCategoriesUncached(recipeCategories, focuses, includeHidden);
	}
	
	private Stream<IRecipeCategory<?>> getRecipeCategoriesUncached(Collection<IRecipeCategory<?>> recipeCategories, IFocusGroup focuses, boolean includeHidden) {
		Stream<IRecipeCategory<?>> categoryStream;
		if (focuses.isEmpty()) {
			if (recipeCategories.isEmpty()) {
				// empty focus, empty recipeCategories => get all recipe categories known to JEI
				categoryStream = this.jeiRecipeCategories.stream();
			} else {
				// empty focus, non-empty recipeCategories => use the recipeCategories
				categoryStream = recipeCategories.stream()
					.distinct();
			}
		} else {
			// focus => get all recipe categories from plugins with the focus
			categoryStream = this.pluginManager.getRecipeTypes(focuses)
				.map(recipeTypeDataMap::get)
				.map(RecipeTypeData::getRecipeCategory);
			
			// non-empty recipeCategories => narrow the results to just ones in recipeCategories
			if (!recipeCategories.isEmpty()) {
				categoryStream = categoryStream.filter(recipeCategories::contains);
			}
		}
		
		if (!includeHidden) {
			categoryStream = categoryStream.filter(c -> !isCategoryHidden(c, focuses));
		}
		
		return categoryStream.sorted(ResourceLocationHolderComparator.create(x -> x.getRecipeType().getUid()));
	}
	
	private <T> Stream<T> getRecipesStream(RecipeType<T> recipeType, IFocusGroup focuses, boolean includeHidden) {
		final var recipeTypeData = this.recipeTypeDataMap.get(recipeType);
		return pluginManager.getRecipes(recipeTypeData, focuses, includeHidden);
	}
	
	private <T> Stream<ITypedIngredient<?>> getRecipeCatalystStream(RecipeType<T> recipeType, boolean includeHidden) {
		final var recipeTypeData = recipeTypeDataMap.get(recipeType);
		final var catalysts = recipeTypeData.getRecipeCategoryCatalysts();
		if (includeHidden) {
			return catalysts.stream();
		}
		return catalysts.stream()
			.filter(ingredientVisibility::isIngredientVisible);
	}
	
	public boolean isRecipeCatalyst(RecipeType<?> recipeType, IFocus<?> focus) {
		final var recipeMap = recipeMaps.get(focus.getRole());
		return recipeMap.isCatalystForRecipeCategory(recipeType, focus.getTypedValue());
	}
	
	public void addPlugins(List<IRecipeManagerPlugin> plugins) {
		if (locked)
			throw new IllegalStateException("Tried to add plugins after registry is locked");
		
		// TODO: add support
		if (!plugins.isEmpty())
			LOGGER.error("Failed to add JEI recipe manager plugins due to being unsupported by TooManyRecipeViewers: [{}]", plugins.stream().map(x -> x.getClass().getName()).collect(Collectors.joining(", ")));
		
//		this.pluginManager.addAll(plugins);
	}
	
	public void addDecorators(ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> decorators) {
		if (locked)
			throw new IllegalStateException("Tried to add plugins after registry is locked");
		
		this.recipeCategoryDecorators = decorators;
	}
	
	public void compact() {
		recipeMaps.values().forEach(RecipeMap::compact);
	}
	//endregion
	
	//region Additional Methods	
	public Stream<RecipeType<?>> getAllRecipeTypes() {
		return jeiRecipeCategories.stream().map(IRecipeCategory::getRecipeType);
	}
	
	public void lock() {
		locked = true;
	}
	//endregion
	
}
