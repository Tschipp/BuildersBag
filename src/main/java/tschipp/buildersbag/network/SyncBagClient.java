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

public class SyncBagClient implements IMessage, IMessageHandler<SyncBagClient, IMessage>
{

	private ItemStack stack;
	private boolean right;
	private String bag;
	
	public SyncBagClient()
	{
	}
	
	public SyncBagClient(ItemStack stack, EnumHand hand)
	{
		this.stack = stack;
		this.bag = stack.getItem().getRegistryName().toString();
		this.right = hand == EnumHand.MAIN_HAND;
	}
	
	@Override
	public IMessage onMessage(SyncBagClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();
		
		mainThread.addScheduledTask(() -> {
			
			EntityPlayer player = Minecraft.getMinecraft().player;
			ItemStack stack = message.right ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
		
			stack.deserializeNBT(message.stack.serializeNBT());
//			((ContainerBag)player.openContainer).processUpdate(stack);
			
		});
		
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		bag = ByteBufUtils.readUTF8String(buf);
		stack = new ItemStack(Item.getByNameOrId(bag));
		stack.deserializeNBT(ByteBufUtils.readTag(buf));
		

		right = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, bag);
		ByteBufUtils.writeTag(buf, stack.serializeNBT());
		buf.writeBoolean(right);
	}

}
