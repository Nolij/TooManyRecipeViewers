package dev.nolij.toomanyrecipeviewers.impl.library.recipes;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.registry.EmiRecipes;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class InternalRecipeManagerPlugin implements IRecipeManagerPlugin {
	
	private final TooManyRecipeViewers runtime;
	
	public InternalRecipeManagerPlugin(TooManyRecipeViewers runtime) {
		this.runtime = runtime;
	}
	
	private List<EmiRecipe> getRecipes(IFocus<?> focus) {
		final var jeiIngredient = focus.getTypedValue();
		final var emiIngredient = JemiUtil.getStack(jeiIngredient);
		final var normalizedEmiStack = emiIngredient.getEmiStacks().getFirst();
		
		return switch (focus.getRole()) {
			case INPUT -> EmiApi.getRecipeManager().getRecipesByInput(normalizedEmiStack);
			case OUTPUT -> EmiApi.getRecipeManager().getRecipesByOutput(normalizedEmiStack);
			case CATALYST -> EmiRecipes.byWorkstation.getOrDefault(normalizedEmiStack, List.of());
			default -> List.of();
		};
	}
	
	@Override
	public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		return getRecipes(focus)
			.stream()
			.map(EmiRecipe::getCategory)
			.distinct()
			.map(runtime::recipeCategory)
			.map(TooManyRecipeViewers.RecipeCategory::getJEIRecipeType)
			.filter(Objects::nonNull)
			.map((Function<RecipeType<?>, RecipeType<?>>) x -> x)
			.toList();
	}
	
	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> jeiCategory, IFocus<V> focus) {
		final var category = runtime.recipeCategory(jeiCategory);
		final var emiCategory = category.getEMICategory();
		
		//noinspection unchecked
		return getRecipes(focus)
			.stream()
			.filter(x -> x.getCategory() == emiCategory)
			.map(x -> runtime.recipe(category, x).getJEIRecipe())
			.filter(Objects::nonNull)
			.map(x -> (T) x)
			.toList();
	}
	
	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> jeiCategory) {
		final var category = runtime.recipeCategory(jeiCategory);
		final var emiCategory = category.getEMICategory();
		final var emiRecipes = EmiApi.getRecipeManager().getRecipes(emiCategory);
		
		//noinspection unchecked
		return emiRecipes
			.stream()
			.map(x -> runtime.recipe(category, x).getJEIRecipe())
			.filter(Objects::nonNull)
			.filter(jeiCategory.getRecipeType().getRecipeClass()::isInstance)
			.map(x -> (T) x)
			.toList();
	}
	
}
