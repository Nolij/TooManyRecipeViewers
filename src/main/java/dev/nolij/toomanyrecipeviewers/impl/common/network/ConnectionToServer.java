package dev.nolij.toomanyrecipeviewers.impl.common.network;

//? if >=21.1 {
import mezz.jei.common.network.packets.PlayToServerPacket;
import org.jetbrains.annotations.NotNull;
//?} else {
/*import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.Constants;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.NetworkHooks;
*///?}
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiRecipeFiller;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConnectionToServer implements IConnectionToServer {
	
	private final boolean serverHasJEI;
	
	public ConnectionToServer() {
		final var clientPacketListener = Minecraft.getInstance().getConnection();
		if (clientPacketListener == null ||
			clientPacketListener.getConnection().isMemoryConnection()) {
			serverHasJEI = false;
		} else {
			//? if >=21.1 {
			serverHasJEI = clientPacketListener.hasChannel(PacketRecipeTransfer.TYPE);
			//?} else {
			/*final var connection = clientPacketListener.getConnection();
			final var connectionData = NetworkHooks.getConnectionData(connection);
			if (connectionData == null) {
				serverHasJEI = false;
			} else {
				final var channels = connectionData.getChannels();
				serverHasJEI = channels.containsKey(Constants.NETWORK_CHANNEL_ID);
			}
			*///?}
		}
	}
	
	@Override
	public boolean isJeiOnServer() {
		return true;
	}
	
	@Override
	//? if >=21.1 {
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(@NotNull T packet) {
	//?} else
	/*public void sendPacketToServer(PacketJei packet) {*/
		if (!(packet instanceof PacketRecipeTransfer recipeTransferPacket))
			return;
		
		if (serverHasJEI) {
			//? if >=21.1 {
			PacketDistributor.sendToServer(recipeTransferPacket);
			//?} else {
			/*final var packetData = packet.getPacketData();
			final var payload = NetworkDirection.PLAY_TO_SERVER.buildPacket(packetData, Constants.NETWORK_CHANNEL_ID);
			PacketDistributor.SERVER.noArg().send(payload.getThis());
			*///?}
			return;
		}
		
		handle(recipeTransferPacket);
	}
	
	public static void handle(PacketRecipeTransfer recipeTransferPacket) {
		final var screen = Minecraft.getInstance().screen;
		if (!(screen instanceof AbstractContainerScreen<?> containerScreen))
			return;
		
		final var containerMenu = containerScreen.getMenu();
		final var inventorySlots = recipeTransferPacket.inventorySlots.stream()
			//? if <21.1
			/*.map(x -> x.index)*/
			.map(containerMenu::getSlot).toList();
		final var craftingSlots = recipeTransferPacket.craftingSlots.stream()
			//? if <21.1
			/*.map(x -> x.index)*/
			.map(containerMenu::getSlot).toList();
		
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
		
		if (EmiClient.onServer) {
			EmiNetwork.sendToServer(new FillRecipeC2SPacket(containerMenu, 0, inventorySlots, craftingSlots, null, stacks));
		} else {
			//noinspection rawtypes,unchecked
			EmiRecipeFiller.clientFill(new StandardRecipeHandler() {
				@Override
				public List<Slot> getInputSources(AbstractContainerMenu handler) {
					return inventorySlots;
				}
				
				@Override
				public List<Slot> getCraftingSlots(AbstractContainerMenu handler) {
					return craftingSlots;
				}
				
				@Override
				public boolean supportsRecipe(EmiRecipe recipe) {
					return true;
				}
			}, null, containerScreen, stacks, EmiCraftContext.Destination.NONE);
		}
	}
	
}
