package dev.nolij.toomanyrecipeviewers.util;

import mezz.jei.api.ingredients.IIngredientType;

public interface IJEMIStack<T> {
	
	IIngredientType<T> tmrv$getType();
	
	T tmrv$getIngredient();
	
}
