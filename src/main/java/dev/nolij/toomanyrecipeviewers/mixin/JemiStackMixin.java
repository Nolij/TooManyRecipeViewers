package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.jemi.JemiStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("NonExtendableApiUsage")
@Mixin(value = JemiStack.class, remap = false)
public class JemiStackMixin<T> implements ITypedIngredient<T> {
	
	@Shadow @Final private IIngredientType<T> type;
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
