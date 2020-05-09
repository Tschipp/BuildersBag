package tschipp.buildersbag.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;

public class SetHeldItemClientHandler implements IMessageHandler<SetHeldItemClient, IMessage>
{
	@Override
	public IMessage onMessage(SetHeldItemClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();
		
		mainThread.addScheduledTask(() -> {
			
			EntityPlayer player = BuildersBag.proxy.getPlayer();
			EnumHand hand = message.right ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			
			player.setHeldItem(hand, message.stack);
			
		});
		
		return null;
	}
}
