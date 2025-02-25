package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.api.stack.ItemEmiStack;
import dev.nolij.toomanyrecipeviewers.util.ITMRVHashable;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = ItemEmiStack.class, remap = false)
public class ItemEmiStackMixin implements ITMRVHashable {
	
	@Shadow @Final private Item item;
	@Shadow @Final private DataComponentPatch componentChanges;
	
	@Override
	public int tmrv$hash() {
		return ITMRVHashable.hash(item, componentChanges, ((ItemEmiStack) (Object) this).getAmount());
	}
	
}
