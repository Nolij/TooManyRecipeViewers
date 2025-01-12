package dev.nolij.toomanyrecipeviewers.impl.api.registration;

import dev.emi.emi.jemi.runtime.JemiBookmarkOverlay;
import dev.emi.emi.jemi.runtime.JemiIngredientFilter;
import dev.emi.emi.jemi.runtime.JemiIngredientListOverlay;
import dev.emi.emi.jemi.runtime.JemiRecipesGui;
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

public class RuntimeRegistration implements IRuntimeRegistration {
	
	private final IRecipeManager recipeManager;
	private final IJeiHelpers jeiHelpers;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IScreenHelper screenHelper;
	
	public RuntimeRegistration(
		IRecipeManager recipeManager,
		IJeiHelpers jeiHelpers,
		IEditModeConfig editModeConfig,
		IIngredientManager ingredientManager,
		IRecipeTransferManager recipeTransferManager,
		IScreenHelper screenHelper
	) {
		this.recipeManager = recipeManager;
		this.jeiHelpers = jeiHelpers;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
		this.recipeTransferManager = recipeTransferManager;
		this.screenHelper = screenHelper;
	}
	
	private IIngredientListOverlay ingredientListOverlay = new JemiIngredientListOverlay();
	private IBookmarkOverlay bookmarkOverlay = new JemiBookmarkOverlay();
	private IRecipesGui recipesGui = new JemiRecipesGui();
	private IIngredientFilter ingredientFilter = new JemiIngredientFilter();
	
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
