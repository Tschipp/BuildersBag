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

public class SetWorkStateClient implements IMessage, IMessageHandler<SetWorkStateClient, IMessage>
{

	private String bag;
	private boolean start;

	public SetWorkStateClient()
	{
	}

	public SetWorkStateClient(String bag, boolean start)
	{
		this.bag = bag;
		this.start = start;
	}

	@Override
	public IMessage onMessage(SetWorkStateClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(() ->
		{
			if (message.start)
				BuildersBag.proxy.startWorking(message.bag, BuildersBag.proxy.getPlayer());
			else
				BuildersBag.proxy.stopWorking(message.bag, BuildersBag.proxy.getPlayer());
		});

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		bag = ByteBufUtils.readUTF8String(buf);
		start = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, bag);
		buf.writeBoolean(start);
	}

}
