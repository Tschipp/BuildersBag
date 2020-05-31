package tschipp.buildersbag.network.server;

import baubles.api.BaublesApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.cache.CacheUpdaterThread;
import tschipp.buildersbag.common.item.BuildersBagItem;

public class RequestCacheUpdateServer implements IMessage, IMessageHandler<RequestCacheUpdateServer, IMessage>
{

	private static final CacheUpdaterThread thread = new CacheUpdaterThread("BuildersBag-Cache-Thread");

	static
	{
		thread.start();
	}

	private int bagSlot;
	private boolean isBauble;
	private ItemStack forStack;
	private int preferred;

	@Deprecated
	public RequestCacheUpdateServer()
	{
	}

	public RequestCacheUpdateServer(int bagSlot, boolean isBauble, ItemStack forStack, int preferredAmount)
	{
		this.bagSlot = bagSlot;
		this.isBauble = isBauble;
		this.forStack = forStack;
		this.preferred = preferredAmount;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		bagSlot = buf.readInt();
		isBauble = buf.readBoolean();
		forStack = ByteBufUtils.readItemStack(buf);
		preferred = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(bagSlot);
		buf.writeBoolean(isBauble);
		ByteBufUtils.writeItemStack(buf, forStack);
		buf.writeInt(preferred);
	}

	@Override
	public IMessage onMessage(RequestCacheUpdateServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener) ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(() ->
		{
			thread.enqueueRunnable(() ->
			{
				EntityPlayer player = ctx.getServerHandler().player;

				ItemStack bag = ItemStack.EMPTY;
				if (message.bagSlot >= 0)
				{
					if (message.isBauble)
					{
						if (Loader.isModLoaded("baubles"))
						{
							bag = BaublesApi.getBaubles(player).getStackInSlot(message.bagSlot);
						}
					}
					else
						bag = player.inventory.getStackInSlot(message.bagSlot);
				}

				if (!bag.isEmpty() && bag.getItem() instanceof BuildersBagItem)
				{
					int amount = BagCache.updateCachedBagStack(bag, player, message.forStack, message.preferred);
				}
			});
			
			

		});

		return null;
	}

}
