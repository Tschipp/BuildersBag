package tschipp.buildersbag.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SetHeldItemClient implements IMessage
{

	public ItemStack stack;
	public boolean right;
	public String bag;
	
	public SetHeldItemClient()
	{
	}
	
	public SetHeldItemClient(ItemStack stack, EnumHand hand)
	{
		this.stack = stack;
		this.bag = stack.getItem().getRegistryName().toString();
		this.right = hand == EnumHand.MAIN_HAND;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		stack = ByteBufUtils.readItemStack(buf);
		right = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeItemStack(buf, stack);
		buf.writeBoolean(right);
	}

}
