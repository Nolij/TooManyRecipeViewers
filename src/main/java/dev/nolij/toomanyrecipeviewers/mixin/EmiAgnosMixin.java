package dev.nolij.toomanyrecipeviewers.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.emi.emi.platform.EmiAgnos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EmiAgnos.class)
public class EmiAgnosMixin {
	
	@WrapMethod(method = "isModLoaded")
	private static boolean tmrv$isModLoaded(String id, Operation<Boolean> original) {
		if (id.equals("jei"))
			return false; // suppress JEMI
		
		return original.call(id);
	}
	
}
