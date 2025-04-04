package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;

public interface IJEITypedItemStack {
	
	Item tmrv$getItem();
	
	default /*? if >=21.1 {*/ DataComponentPatch /*?} else {*/ /*CompoundTag *//*?}*/ tmrv$getDataComponentPatch() {
		return /*? if >=21.1 {*/ DataComponentPatch.EMPTY /*?} else {*/ /*null *//*?}*/;
	}
	
	default long tmrv$getCount() {
		return 1;
	}
	
}
