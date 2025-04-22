package dev.nolij.toomanyrecipeviewers.mixin;

//? if <21.1 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
*///?}
import dev.nolij.toomanyrecipeviewers.util.IItemStackish;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/NormalizedTypedItem", remap = false)
public abstract class NormalizedTypedItemMixin implements IItemStackish<TypedItemStack> {
	
	//? if <21.1 {
	/*@Shadow @Final private Holder<Item> itemHolder;
	
	@Override
	public Item tmrv$getItem() {
		return itemHolder.value();
	}
	*///?}
	
}
