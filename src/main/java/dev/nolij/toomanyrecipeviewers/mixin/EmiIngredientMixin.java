package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.nolij.toomanyrecipeviewers.util.ITMRVHashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = EmiIngredient.class, remap = false)
public interface EmiIngredientMixin extends ITMRVHashable {
	
	@Shadow List<EmiStack> getEmiStacks();
	
	@Override
	default int tmrv$hash() {
		return ITMRVHashable.hash(getEmiStacks());
	}
	
}
