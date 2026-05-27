package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.registry.EmiRecipes;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiRecipes.class, remap = false)
public class EmiRecipesMixin {
	
	@Inject(method = "bake", at = @At("TAIL"))
	private static void tmrv$bake$TAIL(CallbackInfo ci) {
		final var runtime = TooManyRecipeViewers.runtime;
		if (runtime != null) {
			runtime.recipesBaked();
		}
	}
	
}
