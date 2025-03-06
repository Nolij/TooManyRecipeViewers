package dev.nolij.toomanyrecipeviewers.mixin;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CreateRecipeCategory.class, remap = false)
public class CreateRecipeCategoryMixin {
	
	@Inject(method = "withImprovedVisibility(Lnet/neoforged/neoforge/fluids/FluidStack;)Lnet/neoforged/neoforge/fluids/FluidStack;", at = @At("HEAD"), cancellable = true, require = 0)
	private static void tmrv$withImprovedVisibility$HEAD(FluidStack stack, CallbackInfoReturnable<FluidStack> cir) {
		cir.setReturnValue(stack.copy());
	}
	
}
