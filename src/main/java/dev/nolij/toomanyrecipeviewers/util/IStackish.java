package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.core.component.DataComponentPatch;

public interface IStackish {
	
	default /*? if >=21.1 {*/ DataComponentPatch /*?} else {*/ /*CompoundTag *//*?}*/ tmrv$getDataComponentPatch() {
		return /*? if >=21.1 {*/ DataComponentPatch.EMPTY /*?} else {*/ /*null *//*?}*/;
	}
	
	default long tmrv$getCount() {
		return 1;
	}
	
}
