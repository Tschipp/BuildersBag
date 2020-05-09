package tschipp.buildersbag.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.CapHelper;

public class SyncBagCapClientHandler implements IMessageHandler<SyncBagCapClient, IMessage>
{

	@Override
	public IMessage onMessage(SyncBagCapClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();
		
		mainThread.addScheduledTask(() -> {
			
			EntityPlayer player = BuildersBag.proxy.getPlayer();
			ItemStack stack = message.right ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
			
			IBagCap oldCap = CapHelper.getBagCap(stack);
			BagCapProvider.BAG_CAPABILITY.readNBT(oldCap, null, message.readTag);
		});
		
		return null;
	}
	
}
