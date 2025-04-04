package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
//? if >=21.1
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
//? if >=21.1
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TypedItemStack.class, remap = false)
public abstract class TypedItemStackMixin implements IJEITypedItemStack {
	
	//? if >=21.1 {
	@Shadow protected abstract Item getItem();
	
	@Override
	public Item tmrv$getItem() {
		return getItem();
	}
	//?}
	
}
