package dev.nolij.toomanyrecipeviewers.impl.runtime;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BookmarkOverlay implements IBookmarkOverlay {
	
	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		return Optional.empty();
	}
	
	@Override
	public <T> @Nullable T getIngredientUnderMouse(IIngredientType<T> iIngredientType) {
		return null;
	}
	
}
