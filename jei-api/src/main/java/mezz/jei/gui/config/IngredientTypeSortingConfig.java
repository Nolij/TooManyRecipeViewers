package mezz.jei.gui.config;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Comparator;

public class IngredientTypeSortingConfig {
	
	public static String getIngredientTypeString(IListElementInfo<?> info) {
		ITypedIngredient<?> typedIngredient = info.getTypedIngredient();
		return getIngredientTypeString(typedIngredient.getType());
	}
	
	public static String getIngredientTypeString(IIngredientType<?> ingredientType) {
		return ingredientType.getIngredientClass().getName();
	}
	
	public Comparator<IListElementInfo<?>> getComparatorFromMappedValues(Collection<String> allMappedValues) {
		return Comparator.comparing(IngredientTypeSortingConfig::getIngredientTypeString);
	}
	
}
