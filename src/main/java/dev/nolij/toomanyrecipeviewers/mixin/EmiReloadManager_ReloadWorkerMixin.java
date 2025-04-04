package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.registry.EmiPluginContainer;
import dev.nolij.toomanyrecipeviewers.EMIPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev/emi/emi/runtime/EmiReloadManager$ReloadWorker", remap = false)
public class EmiReloadManager_ReloadWorkerMixin {
	
	@Inject(method = "entrypointPriority", at = @At("HEAD"), cancellable = true)
	private static void tmrv$entrypointPriority$HEAD(EmiPluginContainer container, CallbackInfoReturnable<Integer> cir) {
		if (container.plugin() instanceof EMIPlugin)
			cir.setReturnValue(2);
	}
	
}
