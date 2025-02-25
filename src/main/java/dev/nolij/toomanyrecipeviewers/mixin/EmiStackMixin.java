package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.nolij.toomanyrecipeviewers.util.ITMRVHashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EmiStack.class, remap = false)
public abstract class EmiStackMixin implements ITMRVHashable {
	
	@Shadow public abstract Object getKey();
	
	@Override
	public int tmrv$hash() {
		return ITMRVHashable.hash(getKey());
	}
	
}
