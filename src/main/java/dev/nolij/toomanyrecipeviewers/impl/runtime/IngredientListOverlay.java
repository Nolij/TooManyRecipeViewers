package dev.nolij.toomanyrecipeviewers.impl.runtime;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class IngredientListOverlay implements IIngredientListOverlay {
	
	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		return Optional.empty();
	}
	
	@Override
	public <T> @Nullable T getIngredientUnderMouse(IIngredientType<T> iIngredientType) {
		return null;
	}
	
	@Override
	public boolean isListDisplayed() {
		return false;
	}
	
	@Override
	public boolean hasKeyboardFocus() {
		return false;
	}
	
	@Override
	public <T> List<T> getVisibleIngredients(IIngredientType<T> iIngredientType) {
		return List.of();
	}
	
}
