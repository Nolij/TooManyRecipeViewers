package dev.nolij.toomanyrecipeviewers.impl.common.network;

import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.platform.EmiClient;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.network.packets.PlayToServerPacket;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class ConnectionToServer implements IConnectionToServer {
	
	@Override
	public boolean isJeiOnServer() {
		return EmiClient.onServer;
	}
	
	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(@NotNull T packet) {
		if (!EmiClient.onServer)
			return; // TODO: use EmiRecipeFiller.clientFill()
		
		if (!(packet instanceof PacketRecipeTransfer recipeTransferPacket))
			return;
		
		final var screen = Minecraft.getInstance().screen;
		if (!(screen instanceof AbstractContainerScreen<?> containerScreen))
			return;
		
		final var containerMenu = containerScreen.getMenu();
		final var inventorySlots = recipeTransferPacket.inventorySlots.stream().map(containerMenu::getSlot).toList();
		final var craftingSlots = recipeTransferPacket.craftingSlots.stream().map(containerMenu::getSlot).toList();
		final var stacks = recipeTransferPacket.transferOperations.stream()
			.map(TransferOperation::inventorySlotId)
			.map(containerMenu::getSlot)
			.map(Slot::getItem)
			.toList();
		
		EmiNetwork.sendToServer(new FillRecipeC2SPacket(containerMenu, 0, inventorySlots, craftingSlots, null, stacks));
	}
	
}
