package tschipp.buildersbag.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.CapHelper;

public class SyncBagCapServer implements IMessage, IMessageHandler<SyncBagCapServer, IMessage>
{
	private IBagCap bagCap;
	public boolean right;
	public CompoundNBT readTag;

	public SyncBagCapServer()
	{
	}

	public SyncBagCapServer(IBagCap bagCap, EnumHand hand)
	{
		this.bagCap = bagCap;
		this.right = hand == EnumHand.MAIN_HAND;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		readTag = ByteBufUtils.readTag(buf);
		right = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, (CompoundNBT) BagCapProvider.BAG_CAPABILITY.writeNBT(bagCap, null));
		buf.writeBoolean(right);
	}

	@Override
	public IMessage onMessage(SyncBagCapServer message, MessageContext ctx)
	{
		final IThreadListener mainThread =(IThreadListener)ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(() -> {

			PlayerEntity player = ctx.getServerHandler().player;
			ItemStack stack = message.right ? player.getHeldItemMainhand() : player.getHeldItemOffhand();

			IBagCap oldCap = CapHelper.getBagCap(stack);
			BagCapProvider.BAG_CAPABILITY.readNBT(oldCap, null, message.readTag);
		});

		return null;
	}

}

