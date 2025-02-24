package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.jemi.JemiStack;
import dev.nolij.toomanyrecipeviewers.util.IJEMIStack;
import mezz.jei.api.ingredients.IIngredientType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = JemiStack.class, remap = false)
public class JemiStackMixin<T> implements IJEMIStack<T> {
	
	@Shadow @Final private IIngredientType<T> type;
	@Shadow @Final public T ingredient;
	
	@Override
	public IIngredientType<T> tmrv$getType() {
		return type;
	}
	
	@Override
	public T tmrv$getIngredient() {
		return ingredient;
	}
	
}
