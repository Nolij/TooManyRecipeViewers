package dev.nolij.toomanyrecipeviewers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "mekanism/client/recipe_viewer/jei/MekanismJEI", remap = false)
public class MekanismJEIMixin {
	
	@Inject(method = "shouldLoad", at = @At("HEAD"), cancellable = true, require = 0)
	private static void tmrv$shouldLoad(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}
	
}
