package tschipp.buildersbag.compat.chisel;

import mod.chiselsandbits.network.ModPacket;
import mod.chiselsandbits.network.ModPacketTypes;
import mod.chiselsandbits.network.packets.PacketChisel;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import tschipp.buildersbag.common.modules.ChiselsBitsModule;

public class ChiselEvents
{

	@Method(modid = "chiselsandbits")
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChiselPacket(ServerCustomPacketEvent event)
	{
		FMLProxyPacket packet = event.getPacket();
		if (packet.channel().equals("ChiselsAndBits"))
		{
			PacketBuffer buffer = new PacketBuffer(packet.payload().copy());

			int id = buffer.readByte();

			ModPacket modPacket;
			try
			{
				modPacket = ModPacketTypes.constructByID(id);
				modPacket.readPayload(buffer);

				if (modPacket instanceof PacketChisel)
				{
					NetHandlerPlayServer serv = (NetHandlerPlayServer) event.getHandler();
					if (!serv.player.removeTag("chiselPacket"))
						ChiselsBitsModule.checkAndProvideBits((PacketChisel) modPacket, serv.player);
				}

			} catch (InstantiationException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

}
