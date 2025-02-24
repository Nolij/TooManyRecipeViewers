package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.IngredientManager;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = JemiUtil.class, remap = false)
public class JemiUtilMixin {
	
	@Unique
	private static void tmrv$withIngredientManager(Consumer<IngredientManager> consumer, Runnable orElse) {
		final var runtime = TooManyRecipeViewers.runtime;
		if (runtime == null) {
			orElse.run();
			return;
		}
		
		final var ingredientManager = runtime.ingredientManager;
		if (ingredientManager == null) {
			orElse.run();
			return;
		}
		
		consumer.accept(ingredientManager);
	}
	
	@Inject(method = "getStack(Lmezz/jei/api/ingredients/ITypedIngredient;)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"), cancellable = true)
	private static <T> void tmrv$getStack_typedingredient$HEAD(ITypedIngredient<T> typedIngredient, CallbackInfoReturnable<EmiStack> cir) {
		tmrv$withIngredientManager(
			ingredientManager -> cir.setReturnValue(ingredientManager.getEMIStack(typedIngredient)),
			() -> cir.setReturnValue(EmiStack.EMPTY)
		);
	}
	
	@Inject(method = "getStack(Lmezz/jei/api/ingredients/IIngredientType;Ljava/lang/Object;)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"), cancellable = true)
	private static <T> void tmrv$getStack_type_ingredient$HEAD(IIngredientType<T> type, T ingredient, CallbackInfoReturnable<EmiStack> cir) {
		tmrv$withIngredientManager(
			ingredientManager -> cir.setReturnValue(ingredientManager.getEMIStack(type, ingredient)),
			() -> cir.setReturnValue(EmiStack.EMPTY)
		);
	}
	
	@Inject(method = "getTyped", at = @At("HEAD"), cancellable = true)
	private static void tmrv$getTyped$HEAD(EmiStack emiStack, CallbackInfoReturnable<Optional<ITypedIngredient<?>>> cir) {
		tmrv$withIngredientManager(
			ingredientManager -> cir.setReturnValue(ingredientManager.getTypedIngredient(emiStack)),
			() -> cir.setReturnValue(Optional.empty())
		);
	}
	
}
