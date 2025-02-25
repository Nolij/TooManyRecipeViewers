package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.ITMRVHashable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.library.ingredients.TypedIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(value = TypedIngredient.class, remap = false)
public abstract class TypedIngredientMixin<T> implements ITMRVHashable {
	
	@Shadow public abstract IIngredientType<T> getType();
	
	@Shadow public abstract T getIngredient();
	
	@Override
	public int tmrv$hash() {
		return Objects.hash(getType().getUid(), getIngredient());
	}
	
}
