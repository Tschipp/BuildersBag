package tschipp.buildersbag.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;

public class SyncBagCapClient implements IMessage
{

	private IBagCap bagCap;
	public boolean right;
	public NBTTagCompound readTag;
	
	public SyncBagCapClient()
	{
	}
	
	public SyncBagCapClient(IBagCap bagCap, EnumHand hand)
	{
		this.bagCap = bagCap;
		this.right = hand == EnumHand.MAIN_HAND;
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
