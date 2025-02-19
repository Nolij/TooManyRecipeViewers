package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;

import java.util.Objects;

public interface IJEITypedItemStack extends IJEITypedIngredient {
	
	Item tmrv$getItem();
	
	default DataComponentPatch tmrv$getDataComponentPatch() {
		return DataComponentPatch.EMPTY;
	}
	
	default long tmrv$getCount() {
		return 1;
	}
	
	@Override
	default int tmrv$hashIngredient() {
		return Objects.hash(tmrv$getItem(), tmrv$getDataComponentPatch(), tmrv$getCount());
	}
	
}
