package dev.nolij.toomanyrecipeviewers.impl.api.registration;

import dev.emi.emi.jemi.runtime.JemiBookmarkOverlay;
import dev.emi.emi.jemi.runtime.JemiIngredientFilter;
import dev.emi.emi.jemi.runtime.JemiIngredientListOverlay;
import dev.emi.emi.jemi.runtime.JemiRecipesGui;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import org.jetbrains.annotations.NotNull;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class RuntimeRegistration implements IRuntimeRegistration {
	
	private final IRecipeManager recipeManager;
	private final IJeiHelpers jeiHelpers;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IScreenHelper screenHelper;
	
	public RuntimeRegistration(TooManyRecipeViewers runtime) {
		this.recipeManager = runtime.recipeManager;
		this.jeiHelpers = runtime.jeiHelpers;
		this.editModeConfig = runtime.editModeConfig;
		this.ingredientManager = runtime.ingredientManager;
		this.recipeTransferManager = runtime.recipeTransferManager;
		this.screenHelper = runtime.screenHelper;
	}
	
	private final IIngredientListOverlay ingredientListOverlay = new JemiIngredientListOverlay();
	private final IBookmarkOverlay bookmarkOverlay = new JemiBookmarkOverlay();
	private final IRecipesGui recipesGui = new JemiRecipesGui();
	private final IIngredientFilter ingredientFilter = new JemiIngredientFilter();
	
	@Override
	public void setIngredientListOverlay(@NotNull IIngredientListOverlay ingredientListOverlay) {
		LOGGER.error(new UnsupportedOperationException());
	}
	
	@Override
	public void setBookmarkOverlay(@NotNull IBookmarkOverlay bookmarkOverlay) {
		LOGGER.error(new UnsupportedOperationException());
	}
	
	@Override
	public void setRecipesGui(@NotNull IRecipesGui recipesGui) {
		LOGGER.error(new UnsupportedOperationException());
	}
	
	@Override
	public void setIngredientFilter(@NotNull IIngredientFilter ingredientFilter) {
		LOGGER.error(new UnsupportedOperationException());
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
