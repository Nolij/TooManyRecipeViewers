package dev.nolij.toomanyrecipeviewers.mixin;

//? if >=21.1 {
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Shadow;
//?}
import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import org.spongepowered.asm.mixin.Mixin;

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
