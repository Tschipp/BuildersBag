package tschipp.buildersbag.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public interface NetworkMessage
{
	public void toBytes(PacketBuffer buf);
	
	public void handle(Supplier<NetworkEvent.Context> ctx);
}
