package tschipp.buildersbag.network.server;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;

public class ModifyPaletteServer implements IMessage, IMessageHandler<ModifyPaletteServer, IMessage>
{

	private ItemStack sel;
	private String uuid;
	private boolean add;
	
	public ModifyPaletteServer()
	{
	}
	
	public ModifyPaletteServer(String uuid, ItemStack selected, boolean add)
	{
		this.uuid = uuid;
		this.sel = selected;
		this.add = add;
	}
	
	@Override
	public IMessage onMessage(ModifyPaletteServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener)ctx.getServerHandler().player.world;
		
		mainThread.addScheduledTask(() -> {
			
			EntityPlayer player = ctx.getServerHandler().player;
			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			
			IBagCap cap;
			if((cap = CapHelper.getBagCap(main)) != null && cap.getUUID().equals(message.uuid));
			else if((cap = CapHelper.getBagCap(off)) != null && cap.getUUID().equals(message.uuid));
			
			List<ItemStack> palette = cap.getPalette();
			if(message.add)
				palette.add(message.sel.copy());
			else
			{
				for(int i = 0; i < palette.size(); i++)
				{
					if(ItemStack.areItemStacksEqual(message.sel, palette.get(i)))
					{
						palette.remove(i);
						break;
					}
				}
			}
		});
		
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		uuid = ByteBufUtils.readUTF8String(buf);
		sel = ByteBufUtils.readItemStack(buf);
		add = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, uuid);
		ByteBufUtils.writeItemStack(buf, sel);
		buf.writeBoolean(add);
	}

}
