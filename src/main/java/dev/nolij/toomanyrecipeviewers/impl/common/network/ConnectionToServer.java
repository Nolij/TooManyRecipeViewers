package dev.nolij.toomanyrecipeviewers.impl.common.network;

import mezz.jei.common.network.IConnectionToServer;
//? if >=1.21.1 {
import mezz.jei.common.network.packets.PlayToServerPacket;
import org.jetbrains.annotations.NotNull;
//?} else
/*import mezz.jei.common.network.packets.PacketJei;*/

public class ConnectionToServer implements IConnectionToServer {
	@Override
	public boolean isJeiOnServer() {
		return false;
	}

	//? if >=1.21.1 {
	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(@NotNull T t) {}
	//?} else {
	/*@Override
	public void sendPacketToServer(PacketJei packetJei) {}
	*///?}
}
