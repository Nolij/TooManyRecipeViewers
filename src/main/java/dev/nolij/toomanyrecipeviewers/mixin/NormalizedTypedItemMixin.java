package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import org.spongepowered.asm.mixin.Mixin;
//? if <21.1 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
*///?}

@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/NormalizedTypedItem", remap = false)
public abstract class NormalizedTypedItemMixin implements IJEITypedItemStack {
	
	//? if <21.1 {
	/*@Shadow @Final private Holder<Item> itemHolder;
	
	@Override
	public Item tmrv$getItem() {
		return itemHolder.value();
	}
	*///?}
	
}
