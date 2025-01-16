package dev.nolij.toomanyrecipeviewers.impl.api.registration;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.plugins.jei.info.IngredientInfoRecipe;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RecipeRegistration implements IRecipeRegistration {
	
	private final IJeiHelpers jeiHelpers;
	private final IIngredientManager ingredientManager;
	private final IRecipeManager recipeManager;
	
	public RecipeRegistration(IJeiHelpers jeiHelpers, IIngredientManager ingredientManager, IRecipeManager recipeManager) {
		this.jeiHelpers = jeiHelpers;
		this.ingredientManager = ingredientManager;
		this.recipeManager = recipeManager;
	}
	
	public IJeiHelpers getJeiHelpers() {
		return this.jeiHelpers;
	}
	
	public IIngredientManager getIngredientManager() {
		return this.ingredientManager;
	}
	
	public IVanillaRecipeFactory getVanillaRecipeFactory() {
		return this.jeiHelpers.getVanillaRecipeFactory();
	}
	
	public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(recipes, "recipes");
		this.recipeManager.addRecipes(recipeType, recipes);
	}
	
	public <T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionComponents, "descriptionComponents");
		this.addIngredientInfo(List.of(ingredient), ingredientType, descriptionComponents);
	}
	
	public <T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionComponents, "descriptionComponents");
		IJeiIngredientInfoRecipe recipe = IngredientInfoRecipe.create(this.ingredientManager, ingredients, ingredientType, descriptionComponents);
		this.addRecipes(RecipeTypes.INFORMATION, List.of(recipe));
	}
	
}
