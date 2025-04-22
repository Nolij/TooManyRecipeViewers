package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.world.item.Item;

public interface IItemStackish<T> extends IStackish<T> {
	
	Item tmrv$getItem();
	
	@Override
	default long tmrv$getAmount() {
		return 1;
	}
	
}
