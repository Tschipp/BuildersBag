//package tschipp.buildersbag.compat.chisel;
//
//import mod.chiselsandbits.network.ModPacket;
//import mod.chiselsandbits.network.ModPacketTypes;
//import mod.chiselsandbits.network.packets.PacketChisel;
//import net.minecraft.network.PacketBuffer;
//import tschipp.buildersbag.common.modules.ChiselsBitsModule;
//
//public class ChiselEvents
//{
//
//	@Method(modid = "chiselsandbits")
//	@SubscribeEvent(priority = EventPriority.HIGHEST)
//	public void onChiselPacket(ServerCustomPacketEvent event)
//	{
//		FMLProxyPacket packet = event.getPacket();
//		if (packet.channel().equals("ChiselsAndBits"))
//		{
//			PacketBuffer buffer = new PacketBuffer(packet.payload().copy());
//
//			int id = buffer.readByte();
//
//			ModPacket modPacket;
//			try
//			{
//				modPacket = ModPacketTypes.constructByID(id);
//				modPacket.readPayload(buffer);
//
//				if (modPacket instanceof PacketChisel)
//				{
//					NetHandlerPlayServer serv = (NetHandlerPlayServer) event.getHandler();
//					if (!serv.player.removeTag("chiselPacket"))
//						ChiselsBitsModule.checkAndProvideBits((PacketChisel) modPacket, serv.player);
//				}
//
//			} catch (InstantiationException | IllegalAccessException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//
//}
