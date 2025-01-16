package dev.nolij.toomanyrecipeviewers.impl.api.recipe.advanced;

import dev.nolij.toomanyrecipeviewers.impl.api.recipe.RecipeManager;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPluginHelper;

public class RecipeManagerPluginHelper implements IRecipeManagerPluginHelper {
	
	private final RecipeManager recipeManager;
	
	public RecipeManagerPluginHelper(RecipeManager recipeManager) {
		this.recipeManager = recipeManager;
	}
	
	@Override
	public boolean isRecipeCatalyst(RecipeType<?> recipeType, IFocus<?> focus) {
		return recipeManager.isRecipeCatalyst(recipeType, focus);
	}
	
}
