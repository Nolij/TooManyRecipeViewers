package dev.nolij.toomanyrecipeviewers.impl.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiCategory;
import dev.emi.emi.jemi.JemiRecipe;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.registry.EmiRecipes;
import dev.nolij.toomanyrecipeviewers.JEIPlugins;
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
import mezz.jei.core.util.Pair;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class RecipeManager implements IRecipeManager {
	
	private static final Map<RecipeType<?>, EmiRecipeCategory> vanillaJEITypeEMICategoryMap = new HashMap<>();
	static {
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.CRAFTING, VanillaEmiRecipeCategories.CRAFTING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.SMELTING, VanillaEmiRecipeCategories.SMELTING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.BLASTING, VanillaEmiRecipeCategories.BLASTING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.SMOKING, VanillaEmiRecipeCategories.SMOKING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.CAMPFIRE_COOKING, VanillaEmiRecipeCategories.CAMPFIRE_COOKING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.STONECUTTING, VanillaEmiRecipeCategories.STONECUTTING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.SMITHING, VanillaEmiRecipeCategories.SMITHING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.ANVIL, VanillaEmiRecipeCategories.ANVIL_REPAIRING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.BREWING, VanillaEmiRecipeCategories.BREWING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.FUELING, VanillaEmiRecipeCategories.FUEL);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.COMPOSTING, VanillaEmiRecipeCategories.COMPOSTING);
		vanillaJEITypeEMICategoryMap.put(RecipeTypes.INFORMATION, VanillaEmiRecipeCategories.INFO);
	}
	
	private final EmiRegistry registry;
	private final List<IRecipeCategory<?>> jeiRecipeCategories;
	private final ImmutableListMultimap<RecipeType<?>, ITypedIngredient<?>> jeiRecipeCatalysts; // TODO: check unused
	private final IIngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	
	private final Map<RecipeType<?>, EmiRecipeCategory> jeiTypeEMICategoryMap = new HashMap<>();
	private final Map<IRecipeCategory<?>, EmiRecipeCategory> jeiCategoryEMICategoryMap = new HashMap<>(); // TODO
	private final Map<EmiRecipeCategory, IRecipeCategory<?>> emiCategoryJEICategoryMap = new HashMap<>();
	private final Map<RecipeType<?>, IRecipeCategory<?>> jeiTypeCategoryMap = new HashMap<>();
	
	private final RecipeTypeDataMap recipeTypeDataMap;
	private final EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps;
	private final Set<RecipeType<?>> hiddenRecipeTypes = new HashSet<>();
	private final PluginManager pluginManager;
	private ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators;
	
	@Nullable
	@Unmodifiable
	private List<IRecipeCategory<?>> recipeCategoriesVisibleCache = null;
	
	public RecipeManager(
		EmiRegistry registry, 
		List<IRecipeCategory<?>> jeiRecipeCategories, 
		ImmutableListMultimap<RecipeType<?>, ITypedIngredient<?>> jeiRecipeCatalysts, 
		IIngredientManager ingredientManager, 
		IIngredientVisibility ingredientVisibility
	) {
		this.registry = registry;
		this.jeiRecipeCategories = jeiRecipeCategories;
		this.jeiRecipeCatalysts = jeiRecipeCatalysts;
		this.ingredientManager = ingredientManager;
		this.ingredientVisibility = ingredientVisibility;
		
		jeiTypeEMICategoryMap.putAll(vanillaJEITypeEMICategoryMap);
		
		Set<ResourceLocation> existingCategories = EmiRecipes.categories.stream().map(EmiRecipeCategory::getId).collect(Collectors.toSet());
		for (var jeiCategory : jeiRecipeCategories) {
			var jeiRecipeType = jeiCategory.getRecipeType();
			jeiTypeCategoryMap.put(jeiRecipeType, jeiCategory);
			var id = jeiRecipeType.getUid();
			
			var jeiCatalysts = jeiRecipeCatalysts.get(jeiRecipeType);
			var emiCatalysts = jeiCatalysts.stream().map(JemiUtil::getStack).toList();
			
			EmiRecipeCategory emiCategory;
			if (jeiTypeEMICategoryMap.containsKey(jeiRecipeType)) {
				emiCategory = jeiTypeEMICategoryMap.get(jeiRecipeType);
			} else {
				if (JEIPlugins.modsWithEMIPlugins.contains(id.getNamespace()) ||
					existingCategories.contains(id)) {
					continue;
				}
				emiCategory = new JemiCategory(jeiCategory);
				registry.addCategory(emiCategory);
			}
			
			jeiTypeEMICategoryMap.put(jeiRecipeType, emiCategory);
			jeiCategoryEMICategoryMap.put(jeiCategory, emiCategory);
			
			for (var emiCatalyst : emiCatalysts) {
				if (!emiCatalyst.isEmpty()) {
					registry.addWorkstation(emiCategory, emiCatalyst);
				}
			}
		}
		jeiCategoryEMICategoryMap.forEach((k, v) -> emiCategoryJEICategoryMap.put(v, k));
		
		this.recipeMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
			final RecipeMap recipeMap = new RecipeMap(ResourceLocationHolderComparator.create(RecipeType::getUid), ingredientManager, role);
			this.recipeMaps.put(role, recipeMap);
		}
		
//		var categorizedJEIRecipeCatalysts = new ListMultiMap<IRecipeCategory<?>, ITypedIngredient<?>>();
//		jeiRecipeCatalysts.forEach((key, value) -> categorizedJEIRecipeCatalysts.put(jeiTypeCategoryMap.get(key), value));
//		recipeTypeDataMap = new RecipeTypeDataMap(jeiRecipeCategories, categorizedJEIRecipeCatalysts.toImmutable());
		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(this.recipeMaps.get(RecipeIngredientRole.CATALYST));
		for (IRecipeCategory<?> recipeCategory : jeiRecipeCategories) {
			RecipeType<?> recipeType = recipeCategory.getRecipeType();
			if (jeiRecipeCatalysts.containsKey(recipeType)) {
				List<ITypedIngredient<?>> catalysts = jeiRecipeCatalysts.get(recipeType);
				recipeCatalystBuilder.addCategoryCatalysts(recipeCategory, catalysts);
			}
		}
		ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> recipeCategoryCatalystsMap = recipeCatalystBuilder.buildRecipeCategoryCatalysts();
		this.recipeTypeDataMap = new RecipeTypeDataMap(jeiRecipeCategories, recipeCategoryCatalystsMap);
		
		// TODO: replace `InternalRecipeManagerPlugin`
		pluginManager = new PluginManager(new InternalRecipeManagerPlugin(ingredientManager, recipeTypeDataMap, recipeMaps));
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
		//noinspection unchecked
		return (IRecipeCategory<T>) jeiTypeCategoryMap.get(recipeType);
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
		if (jeiRecipeType == RecipeTypes.INFORMATION) {
			//noinspection unchecked
			var jeiInfoRecipes = (List<IJeiIngredientInfoRecipe>) jeiRecipes;
			
			for (var jeiInfoRecipe : jeiInfoRecipes) {
				var emiIngredients = jeiInfoRecipe
					.getIngredients()
					.stream()
					.map(JemiUtil::getStack)
					.map(EmiIngredient.class::cast)
					.toList();
				
				var lines = jeiInfoRecipe
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
				
				var emiInfoRecipe = new EmiInfoRecipe(emiIngredients, lines, null);
				registry.addRecipe(emiInfoRecipe);
				recipeCategoriesVisibleCache = null;
			}
			return;
//		} else if (jeiRecipeType == RecipeTypes.CRAFTING) {
//			LOGGER.info("CRAFTING recipes added from {}", new Exception().getStackTrace()[2].getClassName());
		}
		
		final var emiCategory = jeiTypeEMICategoryMap.get(jeiRecipeType);
		
		final var recipeTypeData = recipeTypeDataMap.get(jeiRecipeType);
		final var jeiCategory = recipeTypeData.getRecipeCategory();
//		final var jeiCategory = getRecipeCategory(jeiRecipeType);
		final var hiddenRecipes = recipeTypeData.getHiddenRecipes();
		
		final var addedRecipes = new ArrayList<T>(jeiRecipes.size());
		for (T jeiRecipe : jeiRecipes) {
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
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipeType);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.addAll(recipes);
		recipeCategoriesVisibleCache = null;
	}
	
	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipeType);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
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
		
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		Collection<IRecipeCategoryDecorator<T>> decorators = getRecipeCategoryDecorators(recipeType);
		
		final IScalableDrawable recipeBackground;
		final int borderPadding;
		if (recipeCategory.needsRecipeBorder()) {
			recipeBackground = Internal.getTextures().getRecipeBackground();
			borderPadding = 4;
		} else {
			recipeBackground = DrawableBlank.EMPTY;
			borderPadding = 0;
		}
		
		return RecipeLayout.create(
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
		
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		Collection<IRecipeCategoryDecorator<T>> decorators = getRecipeCategoryDecorators(recipeType);
		
		final IScalableDrawable recipeBackground;
		final int borderPadding;
		if (recipeCategory.needsRecipeBorder()) {
			recipeBackground = Internal.getTextures().getRecipeBackground();
			borderPadding = 4;
		} else {
			recipeBackground = DrawableBlank.EMPTY;
			borderPadding = 0;
		}
		
		return RecipeLayout.create(
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
		
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		Collection<IRecipeCategoryDecorator<T>> decorators = getRecipeCategoryDecorators(recipeType);
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
		RecipeSlotBuilder builder = new RecipeSlotBuilder(ingredientManager, 0, role);
		builder.addOptionalTypedIngredients(ingredients);
		CycleTimer cycleTimer = CycleTimer.create(ingredientCycleOffset);
		Pair<Integer, IRecipeSlotDrawable> result = builder.build(focusedIngredients, cycleTimer);
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
			var emiRecipe = new JemiRecipe<>(emiCategory, jeiCategory, jeiRecipe);
			registry.addRecipe(emiRecipe);
			return true;
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(jeiRecipe, jeiCategory, ingredientManager);
			LOGGER.error("Found a broken recipe, failed to addRecipe: {}\n", recipeInfo, e);
			return false;
		}
	}
	
	@Unmodifiable
	@SuppressWarnings("unchecked")
	public <T> List<IRecipeCategoryDecorator<T>> getRecipeCategoryDecorators(RecipeType<T> recipeType) {
		ImmutableList<IRecipeCategoryDecorator<?>> decorators = recipeCategoryDecorators.get(recipeType);
		return (List<IRecipeCategoryDecorator<T>>) (Object) decorators;
	}
	
	public boolean isCategoryHidden(IRecipeCategory<?> recipeCategory, IFocusGroup focuses) {
		// hide the category if it has been explicitly hidden
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
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
		Stream<?> visibleRecipes = getRecipesStream(recipeType, focuses, false);
		return visibleRecipes.findAny().isEmpty();
	}
	
	public Stream<IRecipeCategory<?>> getRecipeCategoriesForTypes(Collection<RecipeType<?>> recipeTypes, IFocusGroup focuses, boolean includeHidden) {
		List<IRecipeCategory<?>> recipeCategories = recipeTypes.stream()
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
	
	public <T> Stream<T> getRecipesStream(RecipeType<T> recipeType, IFocusGroup focuses, boolean includeHidden) {
		RecipeTypeData<T> recipeTypeData = this.recipeTypeDataMap.get(recipeType);
		return pluginManager.getRecipes(recipeTypeData, focuses, includeHidden);
	}
	
	public <T> Stream<ITypedIngredient<?>> getRecipeCatalystStream(RecipeType<T> recipeType, boolean includeHidden) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipeType);
		List<ITypedIngredient<?>> catalysts = recipeTypeData.getRecipeCategoryCatalysts();
		if (includeHidden) {
			return catalysts.stream();
		}
		return catalysts.stream()
			.filter(ingredientVisibility::isIngredientVisible);
	}
	
	public boolean isRecipeCatalyst(RecipeType<?> recipeType, IFocus<?> focus) {
		RecipeMap recipeMap = recipeMaps.get(focus.getRole());
		return recipeMap.isCatalystForRecipeCategory(recipeType, focus.getTypedValue());
	}
	
	public void addPlugins(List<IRecipeManagerPlugin> plugins) {
		this.pluginManager.addAll(plugins);
	}
	
	public void addDecorators(ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> decorators) {
		this.recipeCategoryDecorators = decorators;
	}
	
	public void compact() {
		recipeMaps.values().forEach(RecipeMap::compact);
	}
	//endregion
	
	//region Additional Methods
	public IRecipeCategory<?> getRecipeType(EmiRecipeCategory emiCategory) {
		return emiCategoryJEICategoryMap.get(emiCategory);
	}
	
	public Stream<RecipeType<?>> getAllRecipeTypes() {
		return jeiRecipeCategories.stream().map(IRecipeCategory::getRecipeType);
	}
	//endregion
	
}
