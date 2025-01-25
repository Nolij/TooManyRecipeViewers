package dev.nolij.toomanyrecipeviewers.impl.library.recipes;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PluginManager {
	
	public final List<IRecipeManagerPlugin> plugins = new ArrayList<>();
	
	public PluginManager(IRecipeManagerPlugin internalRecipeManagerPlugin) {
		plugins.add(internalRecipeManagerPlugin);
	}
	
	public Stream<RecipeType<?>> getRecipeTypes(IFocusGroup focusGroup) {
		return this.plugins.stream().flatMap((p) -> this.getPluginRecipeTypeStream(p, focusGroup)).distinct();
	}
	
	private Stream<RecipeType<?>> getPluginRecipeTypeStream(IRecipeManagerPlugin plugin, IFocusGroup focuses) {
		List<IFocus<?>> allFocuses = focuses.getAllFocuses();
		return allFocuses.stream().flatMap((focus) -> plugin.getRecipeTypes(focus).stream());
	}
	
	private <T> Stream<T> getPluginRecipeStream(IRecipeManagerPlugin plugin, IRecipeCategory<T> recipeCategory, IFocusGroup focuses) {
		if (!focuses.isEmpty()) {
			List<IFocus<?>> allFocuses = focuses.getAllFocuses();
			return allFocuses.stream().flatMap((focus) -> plugin.getRecipes(recipeCategory, focus).stream());
		} else {
			return plugin.getRecipes(recipeCategory).stream();
		}
	}
	
}
