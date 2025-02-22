package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import net.minecraft.core.component.DataComponentPatch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/NormalizedTypedItemStack", remap = false)
public abstract class NormalizedTypedItemStackMixin implements IJEITypedItemStack {
	
	@Shadow @Final private DataComponentPatch dataComponentPatch;
	
	@Override
	public DataComponentPatch tmrv$getDataComponentPatch() {
		return dataComponentPatch;
	}
	
}
