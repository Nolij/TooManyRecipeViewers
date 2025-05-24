package dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime;

import dev.nolij.toomanyrecipeviewers.impl.jei.api.registration.RuntimeRegistration;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiKeyMappings;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import org.jetbrains.annotations.NotNull;

public class JEIRuntime implements IJeiRuntime {
	
	private final IRecipeManager recipeManager;
	private final IRecipesGui recipesGUI;
	private final IIngredientFilter ingredientFilter;
	private final IIngredientListOverlay ingredientListOverlay;
	private final IBookmarkOverlay bookmarkOverlay;
	private final IJeiHelpers jeiHelpers;
	private final IIngredientManager ingredientManager;
	private final IJeiKeyMappings jeiKeyMappings;
	private final IScreenHelper screenHelper;
	private final IRecipeTransferManager recipeTransferManager;
	private final IEditModeConfig editModeConfig;
	private final IJeiConfigManager jeiConfigManager;
	
	public JEIRuntime(RuntimeRegistration registration, IJeiKeyMappings jeiKeyMappings, IJeiConfigManager jeiConfigManager) {
		this.recipeManager = registration.getRecipeManager();
		this.recipesGUI = registration.getRecipesGui();
		this.ingredientFilter = registration.getIngredientFilter();
		this.ingredientListOverlay = registration.getIngredientListOverlay();
		this.bookmarkOverlay = registration.getBookmarkOverlay();
		this.jeiHelpers = registration.getJeiHelpers();
		this.ingredientManager = registration.getIngredientManager();
		this.jeiKeyMappings = jeiKeyMappings;
		this.screenHelper = registration.getScreenHelper();
		this.recipeTransferManager = registration.getRecipeTransferManager();
		this.editModeConfig = registration.getEditModeConfig();
		this.jeiConfigManager = jeiConfigManager;
	}
	
	@Override
	public @NotNull IRecipeManager getRecipeManager() {
		return recipeManager;
	}
	
	@Override
	public @NotNull IRecipesGui getRecipesGui() {
		return recipesGUI;
	}
	
	@Override
	public @NotNull IIngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}
	
	@Override
	public @NotNull IIngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}
	
	@Override
	public @NotNull IBookmarkOverlay getBookmarkOverlay() {
		return bookmarkOverlay;
	}
	
	@Override
	public @NotNull IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}
	
	@Override
	public @NotNull IIngredientManager getIngredientManager() {
		return ingredientManager;
	}
	
	@Override
	public @NotNull IJeiKeyMappings getKeyMappings() {
		return jeiKeyMappings;
	}
	
	@Override
	public @NotNull IScreenHelper getScreenHelper() {
		return screenHelper;
	}
	
	@Override
	public @NotNull IRecipeTransferManager getRecipeTransferManager() {
		return recipeTransferManager;
	}
	
	@Override
	public @NotNull IEditModeConfig getEditModeConfig() {
		return editModeConfig;
	}
	
	@Override
	public @NotNull IJeiConfigManager getConfigManager() {
		return jeiConfigManager;
	}
	
}
