package tschipp.buildersbag.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;

public class OpenBaubleBagServer implements IMessage, IMessageHandler<OpenBaubleBagServer, IMessage>
{

	
	private int bagSlot;

	public OpenBaubleBagServer()
	{
	}
	
	public OpenBaubleBagServer(int bagSlot)
	{
		this.bagSlot = bagSlot;
	}
	
	
	@Override
	public IMessage onMessage(OpenBaubleBagServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener) ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(() -> {

			PlayerEntity player = ctx.getServerHandler().player;

			player.openGui(BuildersBag.instance, 1, player.world, message.bagSlot, 0, 0);

		});

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		bagSlot = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(bagSlot);
	}

}
