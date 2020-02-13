package tschipp.buildersbag.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.inventory.ContainerBag;

public class SyncModuleState implements IMessage, IMessageHandler<SyncModuleState, IMessage>
{

	private NBTTagCompound tag;
	private String name;
	
	public SyncModuleState()
	{
	}
	
	public SyncModuleState(String name, IBagModule module)
	{
		this.name = name;
		this.tag = module.serializeNBT();
	}
	
	@Override
	public IMessage onMessage(SyncModuleState message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener)ctx.getServerHandler().player.world;
		
		mainThread.addScheduledTask(() -> {
			
			EntityPlayer player = ctx.getServerHandler().player;
			((ContainerBag)player.openContainer).updateModule(message.name, message.tag);
			
		});
		
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		tag = ByteBufUtils.readTag(buf);
		name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, tag);
		ByteBufUtils.writeUTF8String(buf, name);
	}

}
