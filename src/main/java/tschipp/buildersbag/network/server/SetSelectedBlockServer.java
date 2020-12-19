package tschipp.buildersbag.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;

public class SetSelectedBlockServer implements IMessage, IMessageHandler<SetSelectedBlockServer, IMessage>
{

	private ItemStack sel;
	private String uuid;
	
	public SetSelectedBlockServer()
	{
	}
	
	public SetSelectedBlockServer(String uuid, ItemStack selected)
	{
		this.uuid = uuid;
		this.sel = selected;
	}
	
	@Override
	public IMessage onMessage(SetSelectedBlockServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener)ctx.getServerHandler().player.world;
		
		mainThread.addScheduledTask(() -> {
			
			PlayerEntity player = ctx.getServerHandler().player;
			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			
			IBagCap cap;
			if((cap = CapHelper.getBagCap(main)) != null && cap.getUUID().equals(message.uuid))
			{
				cap.getSelectedInventory().setStackInSlot(0, message.sel);
			}
			else if((cap = CapHelper.getBagCap(off)) != null && cap.getUUID().equals(message.uuid))
			{
				cap.getSelectedInventory().setStackInSlot(0, message.sel);
			}
		});
		
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		uuid = ByteBufUtils.readUTF8String(buf);
		sel = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, uuid);
		ByteBufUtils.writeItemStack(buf, sel);
	}

}
