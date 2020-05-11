package tschipp.buildersbag.network;

import baubles.api.BaublesApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.CapHelper;

public class SyncBagCapInventoryClient implements IMessage, IMessageHandler<SyncBagCapInventoryClient, IMessage>
{

	private IBagCap bagCap;
	public int slot;
	public NBTTagCompound readTag;
	public boolean isBauble;
	
	public SyncBagCapInventoryClient()
	{
	}
	
	public SyncBagCapInventoryClient(IBagCap bagCap, int slot)
	{
		this(bagCap, slot, false);
	}
	
	public SyncBagCapInventoryClient(IBagCap bagCap, int slot, boolean isBauble)
	{
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
		ByteBufUtils.writeTag(buf, (NBTTagCompound) BagCapProvider.BAG_CAPABILITY.writeNBT(bagCap, null));
		buf.writeInt(slot);
		buf.writeBoolean(isBauble);
	}
	
	@Override
	public IMessage onMessage(SyncBagCapInventoryClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(() -> {

			EntityPlayer player = BuildersBag.proxy.getPlayer();
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
