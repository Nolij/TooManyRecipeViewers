package dev.nolij.toomanyrecipeviewers.util;

import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.TMRVIngredientCollector;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OverrideableIngredientCycler extends IngredientCycler {
	
	private final IngredientManager ingredientManager;
	
	public final TMRVIngredientCollector ingredientCollector;
	private @Nullable TMRVIngredientCollector overrideCollector = null;
	
	public OverrideableIngredientCycler(IngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
		this.ingredientCollector = new TMRVIngredientCollector(ingredientManager);
	}
	
	@Override
	protected List<ITypedIngredient<?>> getDisplayedIngredients() {
		return (overrideCollector == null ? ingredientCollector : overrideCollector).getCollectedIngredients();
	}
	
	public IIngredientConsumer createDisplayOverrides() {
		if (this.overrideCollector == null) {
			this.overrideCollector = new TMRVIngredientCollector(ingredientManager);
			reset();
		}
		
		return this.overrideCollector;
	}
	
	public void clearDisplayOverrides() {
		this.overrideCollector = null;
		reset();
	}
	
}
