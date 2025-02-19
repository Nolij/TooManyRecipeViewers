package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/NormalizedTypedItemStack", remap = false)
public abstract class NormalizedTypedItemStackMixin implements IJEITypedItemStack {
	
	@Shadow @Final private DataComponentPatch dataComponentPatch;
	
	@Shadow protected abstract Item getItem();
	
	@Override
	public Item tmrv$getItem() {
		return getItem();
	}
	
	@Override
	public DataComponentPatch tmrv$getDataComponentPatch() {
		return dataComponentPatch;
	}
	
}
