package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IStackish;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.TypedIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TypedIngredient.class, remap = false)
public class TypedIngredientMixin {
	
	// somehow increases memory usage??
//	@Inject(method = "normalize", at = @At("HEAD"), cancellable = true)
//	private static <T> void tmrv$normalize$HEAD(ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper, CallbackInfoReturnable<ITypedIngredient<T>> cir) {
//		if (typedIngredient instanceof IStackish<?> stackish) {
//			//noinspection unchecked
//			cir.setReturnValue((ITypedIngredient<T>) stackish.tmrv$normalize());
//		}
//	}
	
	@Inject(method =
		//? if >=21.1 {
		"defensivelyCopyTypedIngredientFromApi"
		//?} else
		/*"deepCopy"*/
		, at = @At("HEAD"), cancellable = true)
	private static <T> void tmrv$defensivelyCopyTypedIngredientFromApi$HEAD(IIngredientManager ingredientManager, ITypedIngredient<T> value, CallbackInfoReturnable<ITypedIngredient<T>> cir) {
		if (value instanceof IStackish<?>) {
			cir.setReturnValue(value);
		}
	}
	
}
