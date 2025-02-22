package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import net.minecraft.core.component.DataComponentPatch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/FullTypedItemStack", remap = false)
public abstract class FullTypedItemStackMixin implements IJEITypedItemStack {
	
	@Shadow @Final private DataComponentPatch dataComponentPatch;
	
	@Shadow @Final private int count;
	
	@Override
	public DataComponentPatch tmrv$getDataComponentPatch() {
		return dataComponentPatch;
	}
	
	@Override
	public long tmrv$getCount() {
		return count;
	}
	
}
