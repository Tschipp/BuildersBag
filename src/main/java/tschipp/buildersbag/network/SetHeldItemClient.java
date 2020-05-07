package tschipp.buildersbag.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.common.inventory.ContainerBag;

public class SetHeldItemClient implements IMessage, IMessageHandler<SetHeldItemClient, IMessage>
{

	private ItemStack stack;
	private boolean right;
	private String bag;
	
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
	public IMessage onMessage(SetHeldItemClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();
		
		mainThread.addScheduledTask(() -> {
			
			EntityPlayer player = Minecraft.getMinecraft().player;
			EnumHand hand = message.right ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			
			player.setHeldItem(hand, message.stack);
			
		});
		
		return null;
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
