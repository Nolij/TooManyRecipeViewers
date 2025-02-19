package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/NormalizedTypedItem", remap = false)
public abstract class NormalizedTypedItemMixin implements IJEITypedItemStack {
	
	@Shadow protected abstract Item getItem();
	
	@Override
	public Item tmrv$getItem() {
		return getItem();
	}
	
}
