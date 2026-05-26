package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.impl.ingredient.TMRVStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// yes, a self-mixin is necessary; `ITypedIngredient<T>` "clashes" with `EmiStack`
// (but the JVM doesn't care, so self-mixin go brr)
@SuppressWarnings("NonExtendableApiUsage")
@Mixin(value = TMRVStack.class, remap = false)
public class TMRVStackMixin<T> implements ITypedIngredient<T> {
	
	@Shadow @Final public IIngredientType<T> type;
	@Shadow @Final public T ingredient;
	
	@Override
	public IIngredientType<T> getType() {
		return type;
	}
	
	@Override
	public T getIngredient() {
		return ingredient;
	}
	
}
