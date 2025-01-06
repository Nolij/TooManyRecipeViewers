package dev.nolij.toomanyrecipeviewers.impl.runtime;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;

import java.util.Set;

public class EditModeConfig implements IEditModeConfig {
	
	@Override
	public <V> boolean isIngredientHiddenUsingConfigFile(ITypedIngredient<V> iTypedIngredient) {
		return false;
	}
	
	@Override
	public <V> Set<HideMode> getIngredientHiddenUsingConfigFile(ITypedIngredient<V> iTypedIngredient) {
		return Set.of();
	}
	
	@Override
	public <V> void hideIngredientUsingConfigFile(ITypedIngredient<V> iTypedIngredient, HideMode hideMode) {
		
	}
	
	@Override
	public <V> void showIngredientUsingConfigFile(ITypedIngredient<V> iTypedIngredient, HideMode hideMode) {
		
	}
	
}
