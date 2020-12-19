package tschipp.buildersbag.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.common.inventory.ContainerBag;

public class SyncItemStackServer implements IMessage, IMessageHandler<SyncItemStackServer, IMessage>
{

	private ItemStack stack;
	private boolean right;
	
	public SyncItemStackServer()
	{
	}
	
	public SyncItemStackServer(ItemStack stack, EnumHand hand)
	{
		this.stack = stack;
		this.right = hand == EnumHand.MAIN_HAND;
	}
	
	@Override
	public IMessage onMessage(SyncItemStackServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener)ctx.getServerHandler().player.world;
		
		mainThread.addScheduledTask(() -> {
			
			PlayerEntity player = ctx.getServerHandler().player;
			ItemStack stack = message.right ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
			
			stack.deserializeNBT(message.stack.serializeNBT());
//			((ContainerBag)player.openContainer).processUpdate(stack);
			
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
