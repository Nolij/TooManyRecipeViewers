package dev.nolij.toomanyrecipeviewers.impl.api.recipe;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
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
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.library.util.RecipeErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
	
	private ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators;
	
	private volatile boolean locked = false;
	
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
		
		final var category = runtime.recipeCategory(jeiRecipeType);
		
		for (final var jeiRecipe : jeiRecipes) {
			addRecipe(category, jeiRecipe);
		}
	}
	
	@Override
	public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {}
	
	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {}
	
	@Override
	public void hideRecipeCategory(RecipeType<?> recipeType) {}
	
	@Override
	public void unhideRecipeCategory(RecipeType<?> recipeType) {}
	
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
		final var recipeType = getRecipeType(recipeUid);
		if (recipeType.isEmpty() || recipeType.get().getRecipeClass() != recipeClass)
			return Optional.empty();
		
		//noinspection unchecked
		return Optional.of((RecipeType<T>) recipeType.get());
	}
	
	@Override
	public Optional<RecipeType<?>> getRecipeType(ResourceLocation recipeUid) {
		final var emiRecipe = EmiApi.getRecipeManager().getRecipe(recipeUid);
		if (emiRecipe == null)
			return Optional.empty();
		
		final var emiCategory = emiRecipe.getCategory();
		final var category = runtime.recipeCategory(emiCategory);
		
		return Optional.ofNullable(category.getJEIRecipeType());
	}
	//endregion
	
	//region RecipeManagerInternal
	private final Set<ResourceLocation> replacedRecipeIDs = new HashSet<>();
	private final Set<EmiRecipe> replacementRecipes = new HashSet<>();
	private <T> void addRecipe(TooManyRecipeViewers.RecipeCategory<T> category, T jeiRecipe) {
		final var jeiCategory = category.getJEICategory();
		final var jeiRecipeType = Objects.requireNonNull(jeiCategory).getRecipeType();
		if (!jeiCategory.isHandled(jeiRecipe)) {
			if (LOGGER.isDebugEnabled()) {
				String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(jeiRecipe, jeiCategory, ingredientManager);
				LOGGER.debug("Recipe not added because the recipe category cannot handle it: {}", recipeInfo);
			}
			return;
		}
		
		try {
			final var recipe = runtime.recipe(runtime.recipeCategory(category.getEMICategory()), jeiRecipe);
			final var emiRecipe = recipe.getEMIRecipe();
			registry.addRecipe(emiRecipe);
			if (vanillaJEITypeEMICategoryMap.containsKey(jeiRecipeType) && recipe.getOriginalID() != null) {
				if (emiRecipe instanceof JemiRecipe<?>)
					LOGGER.warn("Recipe replacement for {} will not render properly!", recipe.getOriginalID());
				
				replacementRecipes.add(emiRecipe);
				replacedRecipeIDs.add(recipe.getOriginalID());
			}
		} catch (RuntimeException | LinkageError e) {
			final var recipeInfo = RecipeErrorUtil.getInfoFromRecipe(jeiRecipe, jeiCategory, ingredientManager);
			LOGGER.error("Found a broken recipe, failed to addRecipe: {}\n", recipeInfo, e);
		}
	}
	
	@Unmodifiable
	@SuppressWarnings("unchecked")
	private <T> List<IRecipeCategoryDecorator<T>> getRecipeCategoryDecorators(RecipeType<T> recipeType) {
		final var decorators = recipeCategoryDecorators.get(recipeType);
		return (List<IRecipeCategoryDecorator<T>>) (Object) decorators;
	}
	
	private Stream<IRecipeCategory<?>> getRecipeCategoriesForTypes(Collection<RecipeType<?>> recipeTypes, IFocusGroup focuses, boolean includeHidden) {
		final var recipeCategories = 
			recipeTypes
				.stream()
				.map(runtime::recipeCategory)
				.map(TooManyRecipeViewers.RecipeCategory::getJEICategory)
				.filter(Objects::nonNull)
				.map((Function<IRecipeCategory<?>, IRecipeCategory<?>>) x -> x)
				.toList();
		
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
			categoryStream = getRecipeTypes(focuses)
				.map(x -> runtime.recipeCategory(x).getJEICategory())
				.filter(Objects::nonNull)
				.map(x -> (IRecipeCategory<?>) x);
			
			// non-empty recipeCategories => narrow the results to just ones in recipeCategories
			if (!recipeCategories.isEmpty()) {
				categoryStream = categoryStream.filter(recipeCategories::contains);
			}
		}
		
		return categoryStream.sorted(ResourceLocationHolderComparator.create(x -> x.getRecipeType().getUid()));
	}
	
	private <T> Stream<T> getRecipesStream(RecipeType<T> recipeType, IFocusGroup focuses, boolean includeHidden) {
		return getRecipes(runtime.recipeCategory(recipeType), focuses);
	}
	
	private <T> Stream<ITypedIngredient<?>> getRecipeCatalystStream(RecipeType<T> recipeType, boolean includeHidden) {
		final var category = runtime.recipeCategory(recipeType);
		final var emiCategory = category.getEMICategory();
		final var workstations = EmiApi.getRecipeManager().getWorkstations(emiCategory);
		final var catalysts = workstations
			.stream()
			.map(x -> JemiUtil.getTyped(x.getEmiStacks().getFirst()))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map((Function<ITypedIngredient<?>, ITypedIngredient<?>>) x -> x);
		
		if (includeHidden) {
			return catalysts;
		}
		
		return catalysts
			.filter(ingredientVisibility::isIngredientVisible);
	}
	
	public boolean isRecipeCatalyst(RecipeType<?> recipeType, IFocus<?> focus) {
		final var category = runtime.recipeCategory(recipeType);
		final var emiCategory = category.getEMICategory();
		
		final var emiStack = JemiUtil.getStack(focus.getTypedValue());
		final var normalizedEMIStack = emiStack.getEmiStacks().getFirst();
		
		return EmiApi.getRecipeManager().getWorkstations(emiCategory).contains(normalizedEMIStack);
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
	//endregion
	
	//region Additional Methods	
	private List<EmiRecipe> getRecipes(IFocus<?> focus) {
		final var jeiIngredient = focus.getTypedValue();
		final var emiIngredient = JemiUtil.getStack(jeiIngredient);
		final var normalizedEMIStack = emiIngredient.getEmiStacks().getFirst();
		
		return switch (focus.getRole()) {
			case INPUT -> EmiApi.getRecipeManager().getRecipesByInput(normalizedEMIStack);
			case OUTPUT -> EmiApi.getRecipeManager().getRecipesByOutput(normalizedEMIStack);
			case CATALYST -> EmiRecipes.byWorkstation.getOrDefault(normalizedEMIStack, List.of());
			default -> List.of();
		};
	}
	
	private <V> Stream<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		return getRecipes(focus)
			.stream()
			.map(EmiRecipe::getCategory)
			.distinct()
			.map(runtime::recipeCategory)
			.map(TooManyRecipeViewers.RecipeCategory::getJEIRecipeType)
			.filter(Objects::nonNull)
			.map((Function<RecipeType<?>, RecipeType<?>>) x -> x);
	}
	
	private Stream<RecipeType<?>> getRecipeTypes(IFocusGroup focusGroup) {
		return focusGroup.getAllFocuses().stream().flatMap(this::getRecipeTypes).distinct();
	}
	
	private <T, V> Stream<T> getRecipes(TooManyRecipeViewers.RecipeCategory<T> category, IFocus<V> focus) {
		final var emiCategory = category.getEMICategory();
		
		//noinspection unchecked
		return getRecipes(focus)
			.stream()
			.filter(x -> x.getCategory() == emiCategory)
			.map(x -> runtime.recipe(category, x).getJEIRecipe())
			.filter(Objects::nonNull)
			.map(x -> (T) x);
	}
	
	private <T> Stream<T> getRecipes(TooManyRecipeViewers.RecipeCategory<T> category, IFocusGroup focusGroup) {
		return focusGroup.getAllFocuses().stream().flatMap(x -> getRecipes(category, x)).distinct();
	}
	
	private <T> Stream<T> getRecipes(TooManyRecipeViewers.RecipeCategory<T> category) {
		final var jeiCategory = category.getJEICategory();
		if (jeiCategory == null)
			return Stream.empty();
		final var emiCategory = category.getEMICategory();
		final var emiRecipes = EmiApi.getRecipeManager().getRecipes(emiCategory);
		
		//noinspection unchecked
		return emiRecipes
			.stream()
			.map(x -> runtime.recipe(category, x).getJEIRecipe())
			.filter(Objects::nonNull)
			.filter(jeiCategory.getRecipeType().getRecipeClass()::isInstance)
			.map(x -> (T) x);
	}
	
	public Stream<RecipeType<?>> getAllRecipeTypes() {
		return jeiRecipeCategories.stream().map(IRecipeCategory::getRecipeType);
	}
	
	public void lock() {
		locked = true;
		registry.removeRecipes(x ->
			replacedRecipeIDs.contains(x.getId()) &&
			!replacementRecipes.contains(x));
	}
	//endregion
	
}
