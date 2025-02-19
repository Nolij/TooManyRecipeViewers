package dev.nolij.toomanyrecipeviewers.util;

import java.util.Arrays;
import java.util.Collection;

public interface IJEITypedIngredient {
	
	int tmrv$hashIngredient();
	
	static int hashIngredients(Collection<? extends IJEITypedIngredient> ingredients) {
		return Arrays.hashCode(ingredients.stream().map(IJEITypedIngredient::tmrv$hashIngredient).toArray());
	}
	
}
