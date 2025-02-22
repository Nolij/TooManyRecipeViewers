package dev.nolij.toomanyrecipeviewers.util;

import java.util.Collection;

public interface IJEITypedIngredient {
	
	int tmrv$hashIngredient();
	
	static int hash(IJEITypedIngredient ingredient) {
		if (ingredient == null)
			return 0;
		
		return ingredient.tmrv$hashIngredient();
	}
	
	static int hash(Collection<? extends IJEITypedIngredient> ingredients) {
		return ingredients.stream().map(IJEITypedIngredient::hash).toList().hashCode();
	}
	
}
