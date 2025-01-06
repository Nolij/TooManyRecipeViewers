package dev.nolij.toomanyrecipeviewers.impl.runtime;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class RecipesGUI implements IRecipesGui {
	
	@Override
	public void show(List<IFocus<?>> list) {
		
	}
	
	@Override
	public void showTypes(List<RecipeType<?>> list) {
		
	}
	
	@Override
	public <T> void showRecipes(IRecipeCategory<T> iRecipeCategory, List<T> list, List<IFocus<?>> list1) {
		
	}
	
	@Override
	public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> iIngredientType) {
		return Optional.empty();
	}
	
	@Override
	public Optional<Screen> getParentScreen() {
		return Optional.empty();
	}
	
}
