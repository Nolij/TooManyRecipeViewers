package dev.nolij.toomanyrecipeviewers.impl.recipe;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecipeManager implements IRecipeManager {
	
	@Override
	public <R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType) {
		return null;
	}
	
	@Override
	public IRecipeCategoriesLookup createRecipeCategoryLookup() {
		return null;
	}
	
	@Override
	public <T> IRecipeCategory<T> getRecipeCategory(RecipeType<T> recipeType) {
		return null;
	}
	
	@Override
	public IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType) {
		return null;
	}
	
	@Override
	public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> collection) {
		
	}
	
	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> collection) {
		
	}
	
	@Override
	public <T> void addRecipes(RecipeType<T> recipeType, List<T> list) {
		
	}
	
	@Override
	public void hideRecipeCategory(RecipeType<?> recipeType) {
		
	}
	
	@Override
	public void unhideRecipeCategory(RecipeType<?> recipeType) {
		
	}
	
	@Override
	public <T> IRecipeLayoutDrawable<T> createRecipeLayoutDrawableOrShowError(IRecipeCategory<T> iRecipeCategory, T t, IFocusGroup iFocusGroup) {
		return null;
	}
	
	@Override
	public <T> Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable(IRecipeCategory<T> iRecipeCategory, T t, IFocusGroup iFocusGroup) {
		return Optional.empty();
	}
	
	@Override
	public <T> Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable(IRecipeCategory<T> iRecipeCategory, T t, IFocusGroup iFocusGroup, IScalableDrawable iScalableDrawable, int i) {
		return Optional.empty();
	}
	
	@Override
	public IRecipeSlotDrawable createRecipeSlotDrawable(RecipeIngredientRole recipeIngredientRole, List<Optional<ITypedIngredient<?>>> list, Set<Integer> set, int i) {
		return null;
	}
	
	@Override
	public <T> IIngredientSupplier getRecipeIngredients(IRecipeCategory<T> iRecipeCategory, T t) {
		return null;
	}
	
	@Override
	public <T> Optional<RecipeType<T>> getRecipeType(ResourceLocation resourceLocation, Class<? extends T> aClass) {
		return Optional.empty();
	}
	
	@Override
	public Optional<RecipeType<?>> getRecipeType(ResourceLocation resourceLocation) {
		return Optional.empty();
	}
	
}
