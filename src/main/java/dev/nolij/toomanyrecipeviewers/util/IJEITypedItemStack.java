package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;

public interface IJEITypedItemStack extends ITMRVHashable {
	
	Item tmrv$getItem();
	
	default DataComponentPatch tmrv$getDataComponentPatch() {
		return DataComponentPatch.EMPTY;
	}
	
	default long tmrv$getCount() {
		return 1;
	}
	
	@Override
	default int tmrv$hash() {
		return ITMRVHashable.hash(tmrv$getItem(), tmrv$getDataComponentPatch(), tmrv$getCount());
	}
	
}
