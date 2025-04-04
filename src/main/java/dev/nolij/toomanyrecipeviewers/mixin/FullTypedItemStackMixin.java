package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
//? if <21.1
/*import net.minecraft.world.item.Item;*/
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/FullTypedItemStack", remap = false)
public abstract class FullTypedItemStackMixin implements IJEITypedItemStack {
	
	//? if <21.1 {
	/*@Shadow @Final private Holder<Item> itemHolder;
	
	@Override
	public Item tmrv$getItem() {
		return itemHolder.value();
	}
	*///?}
	
	@Shadow(aliases = "tag") @Final private DataComponentPatch dataComponentPatch;
	
	@Shadow @Final private int count;
	
	@Override
	public /*? if >=21.1 {*/ DataComponentPatch /*?} else {*/ /*CompoundTag *//*?}*/ tmrv$getDataComponentPatch() {
		return dataComponentPatch;
	}
	
	@Override
	public long tmrv$getCount() {
		return count;
	}
	
}
