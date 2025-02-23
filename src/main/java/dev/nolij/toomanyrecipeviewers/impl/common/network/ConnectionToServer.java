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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

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
		
		final var craftingSlotIndex = new int[craftingSlots.size()];
		for (int i = 0; i < craftingSlotIndex.length; i++)
			craftingSlotIndex[i] = craftingSlots[i].index;
		
		final var transferOperationIndex = new ArrayList<Optional<TransferOperation>>(craftingSlots.size());
		for (final var craftingSlotId : craftingSlotIndex) {
			transferOperationIndex.add(recipeTransferPacket.transferOperations
				.stream().filter(x -> x.craftingSlotId() == craftingSlotId)
				.findFirst());
		}
		
		final var stacks = new ArrayList<ItemStack>(craftingSlots.size());
		for (int i = 0; i < craftingSlotIndex.length; i++) {
			stacks.add(transferOperationIndex[i]
				.map(TransferOperation::inventorySlotId)
				.map(containerMenu::getSlot)
				.map(Slot::getItem)
				.map(x -> x.copyWithCount(1))
				.orElse(ItemStack.EMPTY));
		}
		
		EmiNetwork.sendToServer(new FillRecipeC2SPacket(containerMenu, 0, inventorySlots, craftingSlots, null, stacks));
	}
	
}
