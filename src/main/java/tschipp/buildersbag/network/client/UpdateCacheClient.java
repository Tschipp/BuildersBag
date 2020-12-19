package tschipp.buildersbag.network.client;

import baubles.api.BaublesApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.CapHelper;

public class UpdateCacheClient implements IMessage, IMessageHandler<UpdateCacheClient, IMessage>
{
	private int bagSlot;
	private boolean isBauble;
	private ItemStack forStack;
	private int amount;
	
	@Deprecated
	public UpdateCacheClient()
	{
	}

	public UpdateCacheClient(int bagSlot, boolean isBauble, ItemStack forStack, int amount)
	{
		this.bagSlot = bagSlot;
		this.isBauble = isBauble;
		this.forStack = forStack;
		this.amount = amount;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		bagSlot = buf.readInt();
		isBauble = buf.readBoolean();
		forStack = ByteBufUtils.readItemStack(buf);
		amount = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(bagSlot);
		buf.writeBoolean(isBauble);
		ByteBufUtils.writeItemStack(buf, forStack);
		buf.writeInt(amount);
	}
	
	@Override
	public IMessage onMessage(UpdateCacheClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(() -> {

			PlayerEntity player = BuildersBag.proxy.getPlayer();
			
			ItemStack bag = ItemStack.EMPTY;
			if (message.isBauble)
			{
				if (Loader.isModLoaded("baubles"))
				{
					bag = BaublesApi.getBaubles(player).getStackInSlot(message.bagSlot);
				}
			}
			else
				bag = player.inventory.getStackInSlot(message.bagSlot);
			
			BagCache.updateCachedBagStackWithAmount(bag, player, message.forStack, message.amount);
		});
		
		return null;
	}
}
