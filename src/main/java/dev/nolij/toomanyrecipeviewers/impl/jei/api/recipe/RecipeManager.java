package dev.nolij.toomanyrecipeviewers.impl.jei.api.recipe;

//? if >=21.1 {
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.library.gui.recipes.layout.RecipeLayoutDrawableErrored;
import mezz.jei.library.util.IngredientSupplierHelper;
import net.minecraft.world.item.crafting.RecipeHolder;
//?}
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.JemiCategory;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.recipe.EmiCompostingRecipe;
import dev.emi.emi.recipe.EmiCookingRecipe;
import dev.emi.emi.recipe.EmiFuelRecipe;
import dev.emi.emi.recipe.EmiShapedRecipe;
import dev.emi.emi.recipe.EmiShapelessRecipe;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import dev.emi.emi.recipe.EmiStonecuttingRecipe;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipe;
import dev.nolij.toomanyrecipeviewers.mixin.SmithingRecipeCategoryAccessor;
import dev.nolij.toomanyrecipeviewers.impl.recipe.ExtendedSmithingRecipe;
import dev.nolij.toomanyrecipeviewers.util.ResourceLocationHolderComparator;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
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
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.ingredients.CycleTimer;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.util.RecipeErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.MOD_ID;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class RecipeManager implements IRecipeManager, TooManyRecipeViewers.ILockable {
	
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
	private final IngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	
	private ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators;
	
	private volatile boolean locked = false;
	
	public RecipeManager(TooManyRecipeViewers runtime) {
		runtime.lockAfterRegistration(this);
		this.runtime = runtime;
		this.registry = runtime.emiRegistry;
		this.jeiRecipeCategories = runtime.recipeCategories;
		this.ingredientManager = runtime.ingredientManager;
		this.ingredientVisibility = runtime.ingredientVisibility;
		
		for (final var jeiCategory : jeiRecipeCategories) {
			final var jeiRecipeType = jeiCategory.getRecipeType();
			
			final var jeiCatalysts = runtime.recipeCatalysts.get(jeiRecipeType);
			final var emiCatalysts = jeiCatalysts.stream().map(ingredientManager::getEMIStack).toList();
			
			final var category = category(jeiCategory);
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
		return category(recipeType).getJEICategory();
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
		
		final var category = category(jeiRecipeType);
		
		for (final var jeiRecipe : jeiRecipes) {
			addRecipe(category, jeiRecipe);
		}
	}
	
	private final Set<ResourceLocation> hiddenRecipeIDs = Collections.synchronizedSet(new HashSet<>());
	
	private <T> void collectRecipes(RecipeType<T> recipeType, Collection<T> jeiRecipes, Consumer<ResourceLocation> idConsumer) {
		if (locked)
			throw new IllegalStateException();
		
		final var category = category(recipeType);
		final var recipes = jeiRecipes.stream().map(category::recipe).toList();
		recipes.stream()
			.map(Category.Recipe::getID)
			.filter(Objects::nonNull)
			.forEach(idConsumer);
	}
	
	@Override
	public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> jeiRecipes) {
		collectRecipes(recipeType, jeiRecipes, hiddenRecipeIDs::add);
	}
	
	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> jeiRecipes) {
		collectRecipes(recipeType, jeiRecipes, hiddenRecipeIDs::remove);
	}
	
	private final Set<EmiRecipeCategory> hiddenCategories = Collections.synchronizedSet(new HashSet<>());
	
	@Override
	public void hideRecipeCategory(RecipeType<?> recipeType) {
		if (locked)
			throw new IllegalStateException();
		
		hiddenCategories.add(category(recipeType).getEMICategory());
	}
	
	@Override
	public void unhideRecipeCategory(RecipeType<?> recipeType) {
		if (locked)
			throw new IllegalStateException();
		
		hiddenCategories.remove(category(recipeType).getEMICategory());
	}
	
	//? if >=21.1 {
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
	//?}
	
	@Override
	public <T> Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focusGroup) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");
		
		final var recipeType = recipeCategory.getRecipeType();
		final var decorators = getRecipeCategoryDecorators(recipeType);
		
		//? if >=21.1 {
		final IScalableDrawable recipeBackground;
		final int borderPadding;
		if (recipeCategory.needsRecipeBorder()) {
			recipeBackground = Internal.getTextures().getRecipeBackground();
			borderPadding = 4;
		} else {
			recipeBackground = DrawableBlank.EMPTY;
			borderPadding = 0;
		}
		//?}
		
		return 
			RecipeLayout.create(
				recipeCategory,
				decorators,
				recipe,
				focusGroup,
				ingredientManager
				//? if >=21.1 {
				, recipeBackground,
				borderPadding
				//?}
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
	
	//? if >=21.1 {
	@Override
	public <T> IIngredientSupplier getRecipeIngredients(IRecipeCategory<T> jeiCategory, T recipe) {
		return IngredientSupplierHelper.getIngredientSupplier(recipe, jeiCategory, ingredientManager);
	}
	//?}
	
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
		final var category = category(emiCategory);
		
		return Optional.ofNullable(category.getJEIRecipeType());
	}
	//endregion
	
	//region RecipeManagerInternal
	private final Set<ResourceLocation> replacedRecipeIDs = Collections.synchronizedSet(new HashSet<>());
	private final Set<EmiRecipe> replacementRecipes = Collections.synchronizedSet(new HashSet<>());
	private <T> void addRecipe(Category<T> category, T jeiRecipe) {
		if (runtime.ignoredRecipes.contains(jeiRecipe))
			return;
		
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
			final var recipe = category.recipe(jeiRecipe);
			final var emiRecipe = Objects.requireNonNull(recipe.getEMIRecipe());
			registry.addRecipe(emiRecipe);
			if (vanillaJEITypeEMICategoryMap.containsKey(jeiRecipeType) && recipe.getOriginalID() != null) {
				if (emiRecipe instanceof TMRVRecipe<?>)
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
				.map(this::category)
				.map(Category::getJEICategory)
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
				.map(x -> category(x).getJEICategory())
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
		return getRecipes(category(recipeType), focuses);
	}
	
	private <T> Stream<ITypedIngredient<?>> getRecipeCatalystStream(RecipeType<T> recipeType, boolean includeHidden) {
		final var category = category(recipeType);
		final var emiCategory = category.getEMICategory();
		final var workstations = EmiApi.getRecipeManager().getWorkstations(emiCategory);
		final var catalysts = workstations
			.stream()
			.map(x -> ingredientManager.getTypedIngredient(x.getEmiStacks().getFirst()))
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
		final var category = category(recipeType);
		final var emiCategory = category.getEMICategory();
		
		final var emiStack = ingredientManager.getEMIStack(focus.getTypedValue());
		final var normalizedEMIStack = emiStack.getEmiStacks().getFirst();
		
		return EmiApi.getRecipeManager().getWorkstations(emiCategory).contains(normalizedEMIStack);
	}
	
	public void addPlugins(List<IRecipeManagerPlugin> plugins) {
		if (locked)
			throw new IllegalStateException("Tried to add plugins after registry is locked");
		
		for (final var plugin : plugins) {
			var recipeCount = 0;
			for (final var jeiCategory : jeiRecipeCategories) {
				final var recipes = plugin.getRecipes(jeiCategory);
				recipeCount += recipes.size();
				
				//noinspection rawtypes,unchecked
				addRecipes((RecipeType) jeiCategory.getRecipeType(), recipes);
			}
			
			LOGGER.log(
				recipeCount > 0 ? Level.WARN : Level.ERROR, 
				"Registered {} recipe(s) from Recipe Manager Plugin {}. Do not report issues if there are bugs with this feature; it is not supported!", 
				recipeCount, plugin.getClass().getName()
			);
		}
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
		final var emiIngredient = ingredientManager.getEMIStack(jeiIngredient);
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
			.map(this::category)
			.map(Category::getJEIRecipeType)
			.filter(Objects::nonNull)
			.map((Function<RecipeType<?>, RecipeType<?>>) x -> x);
	}
	
	private Stream<RecipeType<?>> getRecipeTypes(IFocusGroup focusGroup) {
		return focusGroup.getAllFocuses().stream().flatMap(this::getRecipeTypes).distinct();
	}
	
	private <T, V> Stream<T> getRecipes(Category<T> category, IFocus<V> focus) {
		final var emiCategory = category.getEMICategory();
		
		return getRecipes(focus)
			.stream()
			.filter(x -> x.getCategory() == emiCategory)
			.map(x -> category.recipe(x).getJEIRecipe())
			.filter(Objects::nonNull);
	}
	
	private <T> Stream<T> getRecipes(Category<T> category, IFocusGroup focusGroup) {
		return focusGroup.getAllFocuses().stream().flatMap(x -> getRecipes(category, x)).distinct();
	}
	
	public Stream<RecipeType<?>> getAllRecipeTypes() {
		return jeiRecipeCategories.stream().map(IRecipeCategory::getRecipeType);
	}
	
	@Override
	public synchronized void lock() throws IllegalStateException {
		if (locked)
			throw new IllegalStateException();
		locked = true;
		
		EmiReloadManager.step(Component.literal("[TMRV] Locking JEI Recipe Registry..."), 100L);
		
		registry.removeRecipes(x ->
			(replacedRecipeIDs.contains(x.getId()) && !replacementRecipes.contains(x)) ||
			hiddenRecipeIDs.contains(x.getId()) ||
			hiddenCategories.contains(x.getCategory()));
		
		replacedRecipeIDs.clear();
		replacementRecipes.clear();
		hiddenRecipeIDs.clear();
		hiddenCategories.clear();
		runtime.ignoredRecipes.clear();
	}
	//endregion
	
	//region Interop
	//region RecipeCategory
	private final Map<RecipeType<?>, Category<?>> jeiRecipeTypeMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<EmiRecipeCategory, Category<?>> emiCategoryMap = Collections.synchronizedMap(new HashMap<>());
	
	public <T> Category<T> category(
		@Nullable IRecipeCategory<T> jeiCategory,
		@Nullable RecipeType<T> jeiRecipeType,
		@Nullable EmiRecipeCategory emiCategory
	) {
		if (jeiCategory == null && jeiRecipeType == null && emiCategory == null)
			throw new IllegalArgumentException();
		
		if (jeiCategory != null && jeiRecipeType == null)
			jeiRecipeType = jeiCategory.getRecipeType();
		
		final Category<T> result;
		if (jeiRecipeType != null && jeiRecipeTypeMap.containsKey(jeiRecipeType)) {
			//noinspection unchecked
			result = (Category<T>) jeiRecipeTypeMap.get(jeiRecipeType);
		} else if (emiCategory != null && emiCategoryMap.containsKey(emiCategory)) {
			//noinspection unchecked
			result = (Category<T>) emiCategoryMap.get(emiCategory);
		} else {
			result = new Category<>();
		}
		
		if (result.jeiCategory == null && jeiCategory != null)
			result.jeiCategory = jeiCategory;
		if (result.jeiRecipeType == null && jeiRecipeType != null)
			result.jeiRecipeType = jeiRecipeType;
		if (result.emiCategory == null && emiCategory != null)
			result.emiCategory = emiCategory;
		
		if (jeiRecipeType != null && !jeiRecipeTypeMap.containsKey(jeiRecipeType))
			jeiRecipeTypeMap.put(jeiRecipeType, result);
		if (emiCategory != null && !emiCategoryMap.containsKey(emiCategory))
			emiCategoryMap.put(emiCategory, result);
		
		return result;
	}
	
	public <T> Category<T> category(@NotNull IRecipeCategory<T> jeiCategory) {
		return category(jeiCategory, null, null);
	}
	public <T> Category<T> category(@NotNull RecipeType<T> jeiRecipeType) {
		return category(null, jeiRecipeType, null);
	}
	public <T> Category<T> category(@NotNull EmiRecipeCategory emiCategory) {
		return category(null, null, emiCategory);
	}
	
	public class Category<T> {
		
		private Category() {}
		
		private @Nullable IRecipeCategory<T> jeiCategory;
		private @Nullable RecipeType<T> jeiRecipeType;
		private @Nullable EmiRecipeCategory emiCategory;
		
		public synchronized @Nullable IRecipeCategory<T> getJEICategory() {
			if (jeiCategory == null) {
				if (jeiRecipeType != null || emiCategory == null)
					throw new IllegalStateException();
				
				return null;
			}
			
			return jeiCategory;
		}
		
		public synchronized @Nullable RecipeType<T> getJEIRecipeType() {
			if (jeiRecipeType == null) {
				if (jeiCategory != null) {
					jeiRecipeType = jeiCategory.getRecipeType();
					jeiRecipeTypeMap.put(jeiRecipeType, this);
				} else if (emiCategory == null) {
					throw new IllegalStateException();
				} else {
					return null;
				}
			}
			
			return jeiRecipeType;
		}
		
		public synchronized @Nullable EmiRecipeCategory getEMICategory() {
			if (emiCategory == null) {
				if (jeiCategory == null || jeiRecipeType == null)
					return null;
				
				if (vanillaJEITypeEMICategoryMap.containsKey(jeiRecipeType)) {
					emiCategory = vanillaJEITypeEMICategoryMap.get(jeiRecipeType);
				} else {
					emiCategory = new JemiCategory(jeiCategory);
				}
				emiCategoryMap.put(emiCategory, this);
			}
			
			return Objects.requireNonNull(emiCategory);
		}
		
		//region Recipe
		private final Map<T, Recipe> jeiRecipeMap = Collections.synchronizedMap(new HashMap<>());
		private final Map<EmiRecipe, Recipe> emiRecipeMap = Collections.synchronizedMap(new HashMap<>());
		
		public Recipe recipe(@Nullable T jeiRecipe, @Nullable EmiRecipe emiRecipe) {
			if (jeiRecipe == null && emiRecipe == null)
				throw new IllegalArgumentException();
			
			final Recipe result;
			if (jeiRecipe != null && jeiRecipeMap.containsKey(jeiRecipe)) {
				result = jeiRecipeMap.get(jeiRecipe);
			} else if (emiRecipe != null && emiRecipeMap.containsKey(emiRecipe)) {
				result = emiRecipeMap.get(emiRecipe);
			} else {
				result = new Recipe();
			}
			
			if (result.jeiRecipe == null && jeiRecipe != null)
				result.jeiRecipe = jeiRecipe;
			if (result.emiRecipe == null && emiRecipe != null)
				result.emiRecipe = emiRecipe;
			
			if (jeiRecipe != null)
				jeiRecipeMap.put(jeiRecipe, result);
			if (emiRecipe != null)
				emiRecipeMap.put(emiRecipe, result);
			
			return result;
		}
		
		public Recipe recipe(@Nullable Object recipe) {
			if (recipe instanceof EmiRecipe)
				return recipe(null, (EmiRecipe) recipe);
			//noinspection unchecked
			return recipe((T) recipe, null);
		}
		
		public class Recipe {
			
			private Recipe() {}
			
			private @Nullable T jeiRecipe;
			private @Nullable EmiRecipe emiRecipe;
			
			private @Nullable ResourceLocation originalID = null;
			private @Nullable ResourceLocation id = null;
			
			private record ExtractedRecipeData(
				List<EmiIngredient> emiInputs,
				List<EmiStack> emiOutputs,
				boolean shapeless
			) {}
			
			private ExtractedRecipeData extractJEIRecipeData() {
				if (jeiRecipe == null)
					throw new IllegalStateException();
				
				if (jeiRecipe /*? if >=21.1 {*/ instanceof RecipeHolder<?> recipeHolder &&
					recipeHolder.value() /*?}*/ instanceof CraftingRecipe craftingRecipe) {
					if (craftingRecipe instanceof ShapelessRecipe shapelessRecipe) {
						return new ExtractedRecipeData(
							shapelessRecipe.getIngredients().stream().map(EmiIngredient::of).toList(),
							List.of(runtime.ingredientManager.getEMIStack(EmiPort.getOutput(shapelessRecipe))),
							true
						);
					} else if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
						return new ExtractedRecipeData(
							shapedRecipe.getIngredients().stream().map(EmiIngredient::of).toList(),
							List.of(runtime.ingredientManager.getEMIStack(EmiPort.getOutput(shapedRecipe))),
							false
						);
					}
				}
				
				final var jeiCategory = getJEICategory();
				if (jeiCategory == null)
					throw new IllegalStateException();
				
				final var recipeLayoutBuilder = new RecipeLayoutBuilder(ingredientManager);
				jeiCategory.setRecipe(recipeLayoutBuilder, jeiRecipe, runtime.jeiHelpers.getFocusFactory().getEmptyFocusGroup());
				final var data = recipeLayoutBuilder.extractEMIRecipeData();
				
				return new ExtractedRecipeData(data.inputs(), data.outputs(), data.shapeless());
			}
			
			public synchronized @Nullable ResourceLocation getOriginalID() {
				if (originalID == null && jeiRecipe != null && getJEICategory() != null)
					originalID = getJEICategory().getRegistryName(jeiRecipe);
				
				return originalID;
			}
			
			public synchronized @Nullable ResourceLocation getID() {
				if (id == null) {
					if (emiRecipe != null)
						id = emiRecipe.getId();
					
					if (id == null && getOriginalID() != null)
						id = ResourceLocation.fromNamespaceAndPath(MOD_ID, "/" + EmiUtil.subId(getOriginalID()));
				}
				
				return id;
			}
			
			public synchronized @Nullable T getJEIRecipe() {
				if (jeiRecipe == null) {
					if (emiRecipe == null)
						throw new IllegalStateException();

					return null;
				}
				
				return jeiRecipe;
			}
			
			public synchronized @Nullable EmiRecipe getEMIRecipe() {
				if (emiRecipe == null) {
					if (jeiRecipe == null)
						return null;
					
					getOriginalID();
					getID();
					
					if (vanillaJEITypeEMICategoryMap.containsKey(jeiRecipeType)) {
						if (emiCategory == VanillaEmiRecipeCategories.INFO) {
							emiRecipe = convertEMIInfoRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.CRAFTING) {
							emiRecipe = convertEMICraftingRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.SMELTING) {
							emiRecipe = convertEMISmeltingRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.BLASTING) {
							emiRecipe = convertEMIBlastingRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.SMOKING) {
							emiRecipe = convertEMISmokingRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.CAMPFIRE_COOKING) {
							emiRecipe = convertEMICampfireRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.STONECUTTING) {
							emiRecipe = convertEMIStonecuttingRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.SMITHING) {
							emiRecipe = convertEMISmithingRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.ANVIL_REPAIRING) {
							emiRecipe = convertEMIAnvilRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.BREWING) {
							emiRecipe = convertEMIBrewingRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.FUEL) {
							emiRecipe = convertEMIFuelRecipe();
						} else if (emiCategory == VanillaEmiRecipeCategories.COMPOSTING) {
							emiRecipe = convertEMICompostingRecipe();
						}
					}
					
					if (emiRecipe == null) {
						emiRecipe = new TMRVRecipe<>(runtime, Category.this, jeiRecipe, getID());
					}
					
					emiRecipeMap.put(emiRecipe, this);
				}
				
				return emiRecipe;
			}
			
			//region Recipe Converters
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiInfoRecipe convertEMIInfoRecipe() {
				final var jeiRecipe = (IJeiIngredientInfoRecipe) this.jeiRecipe;
				
				final var emiIngredients = jeiRecipe
					.getIngredients()
					.stream()
					.map(ingredientManager::getEMIStack)
					.map(EmiIngredient.class::cast)
					.toList();
				
				final var lines = jeiRecipe
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
				
				return new EmiInfoRecipe(emiIngredients, lines, null);
			}
			
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiCraftingRecipe convertEMICraftingRecipe() {
				final var recipeID = getID();

				//? if >=21.1 {
				@SuppressWarnings("unchecked")
				final var craftingRecipeHolder = (RecipeHolder<CraftingRecipe>) this.jeiRecipe;
				final var craftingRecipe = craftingRecipeHolder.value();
				//?} else
				/*final var craftingRecipe = (CraftingRecipe) this.jeiRecipe;*/
				
				if (craftingRecipe.canCraftInDimensions(3, 3)) {
					if (craftingRecipe instanceof ShapelessRecipe shapelessRecipe) {
						return new EmiShapelessRecipe(shapelessRecipe) {
							@Override
							public ResourceLocation getId() {
								return recipeID;
							}
						};
					} else if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
						return new EmiShapedRecipe(shapedRecipe) {
							@Override
							public ResourceLocation getId() {
								return recipeID;
							}
						};
					}
				}
				
				final var extractedRecipeData = extractJEIRecipeData();
				final var emiInputs = extractedRecipeData.emiInputs;
				final var emiOutputs = extractedRecipeData.emiOutputs;
				
				if (emiOutputs.size() == 1) {
					return new EmiCraftingRecipe(
						emiInputs,
						emiOutputs.getFirst(), 
						recipeID,
						extractedRecipeData.shapeless);
				} else {
					return new EmiPatternCraftingRecipe(emiInputs, EmiStack.EMPTY, recipeID, extractedRecipeData.shapeless) {
						@Override
						public List<EmiStack> getOutputs() {
							return emiOutputs;
						}
						
						@Override
						public SlotWidget getInputWidget(int slot, int x, int y) {
							return new SlotWidget(slot <= emiInputs.size() ? emiInputs.get(slot) : EmiStack.EMPTY, x, y);
						}
						
						@Override
						public SlotWidget getOutputWidget(int x, int y) {
							return new GeneratedSlotWidget(r -> emiOutputs.get(r.nextInt(emiOutputs.size())), unique, x, y);
						}
						
						@Override
						public boolean supportsRecipeTree() {
							return emiOutputs.size() == 1;
						}
					};
				}
			}
			
			private static class EMICookingRecipeWithCustomID extends EmiCookingRecipe {
				
				private final ResourceLocation id;
				
				public EMICookingRecipeWithCustomID(AbstractCookingRecipe recipe, EmiRecipeCategory category, int fuelMultiplier, boolean infiniBurn, ResourceLocation id) {
					super(recipe, category, fuelMultiplier, infiniBurn);
					this.id = id;
				}
				
				@Override
				public ResourceLocation getId() {
					return id;
				}
				
			}
			
			//? if >=21.1
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiCookingRecipe convertEMISmeltingRecipe() {
				//? if >=21.1 {
				@SuppressWarnings("unchecked")
				final var recipe = ((RecipeHolder<SmeltingRecipe>) this.jeiRecipe).value();
				//?} else
				/*final var recipe = (SmeltingRecipe) this.jeiRecipe;*/
				
				return new EMICookingRecipeWithCustomID(recipe, VanillaEmiRecipeCategories.SMELTING, 1, false, getID());
			}
			
			//? if >=21.1
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiCookingRecipe convertEMIBlastingRecipe() {
				//? if >=21.1 {
				@SuppressWarnings("unchecked")
				final var recipe = ((RecipeHolder<BlastingRecipe>) this.jeiRecipe).value();
				//?} else
				/*final var recipe = (BlastingRecipe) this.jeiRecipe;*/
				
				return new EMICookingRecipeWithCustomID(recipe, VanillaEmiRecipeCategories.BLASTING, 2, false, getID());
			}
			
			//? if >=21.1
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiCookingRecipe convertEMISmokingRecipe() {
				//? if >=21.1 {
				@SuppressWarnings("unchecked")
				final var recipe = ((RecipeHolder<SmokingRecipe>) this.jeiRecipe).value();
				//?} else
				/*final var recipe = (SmokingRecipe) this.jeiRecipe;*/
				
				return new EMICookingRecipeWithCustomID(recipe, VanillaEmiRecipeCategories.SMOKING, 2, false, getID());
			}
			
			//? if >=21.1
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiCookingRecipe convertEMICampfireRecipe() {
				//? if >=21.1 {
				@SuppressWarnings("unchecked")
				final var recipe = ((RecipeHolder<CampfireCookingRecipe>) this.jeiRecipe).value();
				//?} else
				/*final var recipe = (CampfireCookingRecipe) this.jeiRecipe;*/
				
				return new EMICookingRecipeWithCustomID(recipe, VanillaEmiRecipeCategories.CAMPFIRE_COOKING, 1, true, getID());
			}
			
			//? if >=21.1
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiStonecuttingRecipe convertEMIStonecuttingRecipe() {
				//? if >=21.1 {
				@SuppressWarnings("unchecked")
				final var recipe = ((RecipeHolder<StonecutterRecipe>) this.jeiRecipe).value();
				//?} else
				/*final var recipe = (StonecutterRecipe) this.jeiRecipe;*/
				
				return new EmiStonecuttingRecipe(recipe) {
					@Override
					public ResourceLocation getId() {
						return getID();
					}
				};
			}
			
			//? if >=21.1
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiRecipe convertEMISmithingRecipe() {
				//? if >=21.1 {
				@SuppressWarnings("unchecked")
				final var recipe = ((RecipeHolder<SmithingRecipe>) this.jeiRecipe).value();
				//?} else
				/*final var recipe = (SmithingRecipe) this.jeiRecipe;*/
				
				final var extension = ((SmithingRecipeCategoryAccessor) runtime.smithingCategory).tmrv$getExtension(recipe);
				if (extension != null) {
					//noinspection rawtypes,unchecked
					return new ExtendedSmithingRecipe(runtime, recipe, extension, getID());
				}
				
				final var id = getID();
				
				LOGGER.warn("Using fallback smithing recipe extractor for recipe {}", id);
				
				final var extractedRecipeData = extractJEIRecipeData();
				final var emiInputs = extractedRecipeData.emiInputs;
				final var emiOutputs = extractedRecipeData.emiOutputs;
				
				return new EmiSmithingRecipe(emiInputs.get(0), emiInputs.get(1), emiInputs.get(2), emiOutputs.getFirst(), getID());
			}
			
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiRecipe convertEMIAnvilRecipe() {
				final var jeiRecipe = (IJeiAnvilRecipe) this.jeiRecipe;
				
				final var id = getID();
				final var leftInputs = jeiRecipe.getLeftInputs().stream().map(ingredientManager::getEMIStack).toList();
				final var rightInputs = jeiRecipe.getRightInputs().stream().map(ingredientManager::getEMIStack).toList();
				final var outputs = jeiRecipe.getOutputs().stream().map(ingredientManager::getEMIStack).toList();
				return new EmiRecipe() {
					private final int unique = EmiUtil.RANDOM.nextInt();
					
					@Override
					public EmiRecipeCategory getCategory() {
						return VanillaEmiRecipeCategories.ANVIL_REPAIRING;
					}
					
					@Override
					public @Nullable ResourceLocation getId() {
						return id;
					}
					
					@Override
					public List<EmiIngredient> getInputs() {
						return Stream.concat(leftInputs.stream().map(EmiIngredient.class::cast), rightInputs.stream()).toList();
					}
					
					@Override
					public List<EmiStack> getOutputs() {
						return outputs;
					}
					
					@Override
					public boolean supportsRecipeTree() {
						return false;
					}
					
					@Override
					public int getDisplayWidth() {
						return 125;
					}
					
					@Override
					public int getDisplayHeight() {
						return 18;
					}
					
					@Override
					public void addWidgets(WidgetHolder widgets) {
						widgets.addTexture(EmiTexture.PLUS, 27, 3);
						widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
						if (leftInputs.size() == 1) widgets.addSlot(leftInputs.getFirst(), 0, 0);
						else widgets.addGeneratedSlot(r -> leftInputs.get(r.nextInt(leftInputs.size())), unique, 0, 0);
						if (rightInputs.size() == 1) widgets.addSlot(rightInputs.getFirst(), 49, 0);
						else widgets.addGeneratedSlot(r -> rightInputs.get(r.nextInt(rightInputs.size())), unique, 49, 0);
						if (outputs.size() == 1) widgets.addSlot(outputs.getFirst(), 107, 0).recipeContext(this);
						else widgets.addGeneratedSlot(r -> outputs.get(r.nextInt(outputs.size())), unique, 107, 0).recipeContext(this);
					}
				};
			}
			
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiBrewingRecipe convertEMIBrewingRecipe() {
				final var jeiRecipe = (IJeiBrewingRecipe) this.jeiRecipe;
				
				return new EmiBrewingRecipe(
					ingredientManager.getEMIStack(jeiRecipe.getPotionInputs().getFirst()),
					ingredientManager.getEMIIngredient(jeiRecipe.getIngredients().stream()),
					ingredientManager.getEMIStack(jeiRecipe.getPotionOutput()),
					getID());
			}
			
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiFuelRecipe convertEMIFuelRecipe() {
				final var jeiRecipe = (IJeiFuelingRecipe) this.jeiRecipe;
				
				return new EmiFuelRecipe(ingredientManager.getEMIIngredient(jeiRecipe.getInputs().stream()), jeiRecipe.getBurnTime(), getID());
			}
			
			@SuppressWarnings("DataFlowIssue")
			private @NotNull EmiCompostingRecipe convertEMICompostingRecipe() {
				final var jeiRecipe = (IJeiCompostingRecipe) this.jeiRecipe;
				
				return new EmiCompostingRecipe(ingredientManager.getEMIIngredient(jeiRecipe.getInputs().stream()), jeiRecipe.getChance(), getID());
			}
			//endregion
			
		}
		//endregion
		
	}
	//endregion
	//endregion
	
}
