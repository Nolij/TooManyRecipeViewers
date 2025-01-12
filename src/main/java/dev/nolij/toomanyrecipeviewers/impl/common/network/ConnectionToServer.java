package dev.nolij.toomanyrecipeviewers.impl.common.network;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PlayToServerPacket;
import org.jetbrains.annotations.NotNull;

public class ConnectionToServer implements IConnectionToServer {
	
	@Override
	public boolean isJeiOnServer() {
		return false;
	}
	
	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(@NotNull T t) {}
	
}
