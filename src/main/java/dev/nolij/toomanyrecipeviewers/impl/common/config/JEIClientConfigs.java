package dev.nolij.toomanyrecipeviewers.impl.common.config;

import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IJeiClientConfigs;

public class JEIClientConfigs implements IJeiClientConfigs {
	
	private final IClientConfig clientConfig = new ClientConfig();
	private final IIngredientFilterConfig ingredientFilterConfig = new IngredientFilterConfig();
	private final IIngredientGridConfig ingredientListConfig = new IngredientGridConfig();
	private final IIngredientGridConfig bookmarkListConfig = new IngredientGridConfig();
	
	@Override
	public IClientConfig getClientConfig() {
		return clientConfig;
	}
	
	@Override
	public IIngredientFilterConfig getIngredientFilterConfig() {
		return ingredientFilterConfig;
	}
	
	@Override
	public IIngredientGridConfig getIngredientListConfig() {
		return ingredientListConfig;
	}
	
	@Override
	public IIngredientGridConfig getBookmarkListConfig() {
		return bookmarkListConfig;
	}
	
}
