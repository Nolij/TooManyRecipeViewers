package dev.nolij.toomanyrecipeviewers.mixin;

import mezz.jei.common.network.packets.PacketRecipeTransfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PacketRecipeTransfer.class)
public interface PacketRecipeTransferAccessor {
    @Accessor("maxTransfer")
    boolean tmrv$isMaxTransfer();
}
