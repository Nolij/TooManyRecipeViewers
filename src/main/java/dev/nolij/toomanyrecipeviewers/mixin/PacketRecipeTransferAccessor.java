package dev.nolij.toomanyrecipeviewers.mixin;

import mezz.jei.common.network.packets.PacketRecipeTransfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PacketRecipeTransfer.class, remap = false)
public interface PacketRecipeTransferAccessor {
    
    @Accessor("maxTransfer") boolean tmrv$isMaxTransfer();
    
}
