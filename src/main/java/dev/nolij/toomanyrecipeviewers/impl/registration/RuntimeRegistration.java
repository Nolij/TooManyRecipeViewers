package dev.nolij.toomanyrecipeviewers.impl.registration;

import dev.nolij.toomanyrecipeviewers.impl.helpers.JEIHelpers;
import dev.nolij.toomanyrecipeviewers.impl.recipe.RecipeManager;
import dev.nolij.toomanyrecipeviewers.impl.recipe.transfer.RecipeTransferManager;
import dev.nolij.toomanyrecipeviewers.impl.runtime.*;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.*;
import org.jetbrains.annotations.NotNull;

public class RuntimeRegistration implements IRuntimeRegistration {
	
	private final IRecipeManager recipeManager = new RecipeManager();
	private final IJeiHelpers jeiHelpers = new JEIHelpers();
	private final IEditModeConfig editModeConfig = new EditModeConfig();
	private final IIngredientManager ingredientManager = new IngredientManager();
	private final IRecipeTransferManager recipeTransferManager = new RecipeTransferManager();
	private final IScreenHelper screenHelper = new ScreenHelper();
	
	private IIngredientListOverlay ingredientListOverlay = new IngredientListOverlay();
	private IBookmarkOverlay bookmarkOverlay = new BookmarkOverlay();
	private IRecipesGui recipesGui = new RecipesGUI();
	private IIngredientFilter ingredientFilter = new IngredientFilter();
	
	@Override
	public void setIngredientListOverlay(@NotNull IIngredientListOverlay ingredientListOverlay) {
		this.ingredientListOverlay = ingredientListOverlay;
	}
	
	@Override
	public void setBookmarkOverlay(@NotNull IBookmarkOverlay bookmarkOverlay) {
		this.bookmarkOverlay = bookmarkOverlay;
	}
	
	@Override
	public void setRecipesGui(@NotNull IRecipesGui recipesGui) {
		this.recipesGui = recipesGui;
	}
	
	@Override
	public void setIngredientFilter(@NotNull IIngredientFilter ingredientFilter) {
		this.ingredientFilter = ingredientFilter;
	}
	
	@Override
	public @NotNull IRecipeManager getRecipeManager() {
		return this.recipeManager;
	}
	
	@Override
	public @NotNull IJeiHelpers getJeiHelpers() {
		return this.jeiHelpers;
	}
	
	@Override
	public @NotNull IIngredientManager getIngredientManager() {
		return this.ingredientManager;
	}
	
	@Override
	public @NotNull IScreenHelper getScreenHelper() {
		return this.screenHelper;
	}
	
	@Override
	public @NotNull IRecipeTransferManager getRecipeTransferManager() {
		return this.recipeTransferManager;
	}
	
	@Override
	public @NotNull IEditModeConfig getEditModeConfig() {
		return this.editModeConfig;
	}
	
	public IIngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}
	
	public IBookmarkOverlay getBookmarkOverlay() {
		return bookmarkOverlay;
	}
	
	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}
	
	public IIngredientFilter getIngredientFilter() {
		return this.ingredientFilter;
	}
	
}
