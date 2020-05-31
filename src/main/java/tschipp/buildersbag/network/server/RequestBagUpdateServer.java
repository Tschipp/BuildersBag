package tschipp.buildersbag.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;

public class RequestBagUpdateServer implements IMessage, IMessageHandler<RequestBagUpdateServer, IMessage>
{

	public int slot;
	public boolean isBauble;
	
	public RequestBagUpdateServer()
	{
	}
	
	public RequestBagUpdateServer(int slot, boolean isBauble)
	{
		this.slot = slot;
		this.isBauble = isBauble;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		slot = buf.readInt();
		isBauble = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(slot);
		buf.writeBoolean(isBauble);
	}
	
	@Override
	public IMessage onMessage(RequestBagUpdateServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener)ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(() -> {

			EntityPlayer player = ctx.getServerHandler().player;
			
			ItemStack bag = InventoryHelper.getStackInSlot(player, message.slot, message.isBauble);
			IBagCap cap = CapHelper.getBagCap(bag);
			
			BuildersBag.network.sendTo(new SyncBagCapInventoryClient(cap, message.slot, message.isBauble), (EntityPlayerMP) player);
		});

		return null;
	}

}
