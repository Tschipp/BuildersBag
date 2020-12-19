package tschipp.buildersbag.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;

public class SetHeldItemClient implements IMessage, IMessageHandler<SetHeldItemClient, IMessage>
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
	
	@Override
	public IMessage onMessage(SetHeldItemClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();
		
		mainThread.addScheduledTask(() -> {
			
			PlayerEntity player = BuildersBag.proxy.getPlayer();
			EnumHand hand = message.right ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			
			player.setHeldItem(hand, message.stack);
			
		});
		
		return null;
	}

}
