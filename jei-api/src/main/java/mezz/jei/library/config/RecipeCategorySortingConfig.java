package mezz.jei.library.config;

import mezz.jei.api.recipe.RecipeType;

import java.util.Collection;
import java.util.Comparator;

public class RecipeCategorySortingConfig {
	
	public Comparator<RecipeType<?>> getComparator(Collection<RecipeType<?>> allValues) {
		return Comparator.comparing(r -> r.getUid().toString());
	}
	
}
