package tschipp.buildersbag.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.inventory.ContainerBag;

public class SyncModuleStateServer implements IMessage, IMessageHandler<SyncModuleStateServer, IMessage>
{

	private CompoundNBT tag;
	private String name;
	
	public SyncModuleStateServer()
	{
	}
	
	public SyncModuleStateServer(String name, IBagModule module)
	{
		this.name = name;
		this.tag = module.serializeNBT();
	}
	
	@Override
	public IMessage onMessage(SyncModuleStateServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener)ctx.getServerHandler().player.world;
		
		mainThread.addScheduledTask(() -> {
			
			PlayerEntity player = ctx.getServerHandler().player;
			if(player.openContainer instanceof ContainerBag)
				((ContainerBag) player.openContainer).updateModule(message.name, message.tag);
			
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
