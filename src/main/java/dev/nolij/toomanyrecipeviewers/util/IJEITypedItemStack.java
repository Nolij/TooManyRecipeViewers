package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;

public interface IJEITypedItemStack {
	
	Item tmrv$getItem();
	
	default DataComponentPatch tmrv$getDataComponentPatch() {
		return DataComponentPatch.EMPTY;
	}
	
	default long tmrv$getCount() {
		return 1;
	}
	
}
