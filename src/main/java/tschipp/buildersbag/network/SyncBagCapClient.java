package tschipp.buildersbag.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.CapHelper;

public class SyncBagCapClient implements IMessage, IMessageHandler<SyncBagCapClient, IMessage>
{

	private IBagCap bagCap;
	private boolean right;
	private NBTTagCompound readTag;
	
	public SyncBagCapClient()
	{
	}
	
	public SyncBagCapClient(IBagCap bagCap, EnumHand hand)
	{
		this.bagCap = bagCap;
		this.right = hand == EnumHand.MAIN_HAND;
	}
	
	@Override
	public IMessage onMessage(SyncBagCapClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();
		
		mainThread.addScheduledTask(() -> {
			
			EntityPlayer player = Minecraft.getMinecraft().player;
			ItemStack stack = message.right ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
			
			IBagCap oldCap = CapHelper.getBagCap(stack);
			BagCapProvider.BAG_CAPABILITY.readNBT(oldCap, null, message.readTag);
		});
		
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		readTag = ByteBufUtils.readTag(buf);
		right = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, (NBTTagCompound) BagCapProvider.BAG_CAPABILITY.writeNBT(bagCap, null));
		buf.writeBoolean(right);
	}

}
