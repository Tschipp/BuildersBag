package tschipp.buildersbag.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;

public class GrowItemClient implements IMessage, IMessageHandler<GrowItemClient, IMessage>
{

	private int growthAmount;
	private EnumHand hand;

	public GrowItemClient()
	{
	}

	public GrowItemClient(int growthAmount, EnumHand hand)
	{
		this.growthAmount = growthAmount;
		this.hand = hand;
	}

	@Override
	public IMessage onMessage(GrowItemClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(() -> {

			EntityPlayer player = BuildersBag.proxy.getPlayer();
			ItemStack s = player.getHeldItem(message.hand);
			s.grow(message.growthAmount);

		});

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		growthAmount = buf.readInt();
		hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(growthAmount);
		buf.writeBoolean(hand == EnumHand.MAIN_HAND);
	}

}
