package dev.nolij.toomanyrecipeviewers.impl.runtime;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;

import java.util.List;

public class IngredientFilter implements IIngredientFilter {
	
	@Override
	public void setFilterText(String s) {
		
	}
	
	@Override
	public String getFilterText() {
		return "";
	}
	
	@Override
	public <T> List<T> getFilteredIngredients(IIngredientType<T> iIngredientType) {
		return List.of();
	}
	
}
