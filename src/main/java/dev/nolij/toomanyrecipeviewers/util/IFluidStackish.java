package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.world.level.material.Fluid;

public interface IFluidStackish<T> extends IStackish<T> {
	
	Fluid tmrv$getFluid();
	
	@Override
	default long tmrv$getAmount() {
		return 1000L;
	}
	
}
