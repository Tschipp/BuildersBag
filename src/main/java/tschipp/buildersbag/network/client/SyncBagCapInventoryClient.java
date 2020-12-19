package tschipp.buildersbag.network.client;

import baubles.api.BaublesApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.data.Tuple;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class SyncBagCapInventoryClient implements IMessage, IMessageHandler<SyncBagCapInventoryClient, IMessage>
{

	private IBagCap bagCap;
	public int slot;
	public CompoundNBT readTag;
	public boolean isBauble;
	
	public SyncBagCapInventoryClient()
	{
	}
		
	public SyncBagCapInventoryClient(IBagCap bagCap, int slot, boolean isBauble)
	{
		if(bagCap == null || bagCap.getBlockInventory() == null)
		{
			BuildersBag.LOGGER.error("Invalid Bag Cap! It is null!");
			new Throwable().printStackTrace();
		}
		
		this.bagCap = bagCap;
		this.slot = slot;
		this.isBauble = isBauble;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		readTag = ByteBufUtils.readTag(buf);
		slot = buf.readInt();
		isBauble = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, (CompoundNBT) BagCapProvider.BAG_CAPABILITY.writeNBT(bagCap, null));
		buf.writeInt(slot);
		buf.writeBoolean(isBauble);
	}
	
	@Override
	public IMessage onMessage(SyncBagCapInventoryClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(() -> {

			PlayerEntity player = BuildersBag.proxy.getPlayer();
			if (message.slot >= 0)
			{
				ItemStack stack = ItemStack.EMPTY;
				if (message.isBauble)
				{
					if (Loader.isModLoaded("baubles"))
					{
						stack = BaublesApi.getBaubles(player).getStackInSlot(message.slot);
					}
				} else
					stack = player.inventory.getStackInSlot(message.slot);

				if (!stack.isEmpty())
				{
					IBagCap oldCap = CapHelper.getBagCap(stack);
					BagCapProvider.BAG_CAPABILITY.readNBT(oldCap, null, message.readTag);
				}
			}
		});

		return null;
	}

}
