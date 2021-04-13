package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.cache.CacheUpdaterThread;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.network.NetworkMessage;

public class RequestCacheUpdateServer implements NetworkMessage
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

	public RequestCacheUpdateServer(PacketBuffer buf)
	{
		bagSlot = buf.readInt();
		isBauble = buf.readBoolean();
		forStack = buf.readItemStack();
		preferred = buf.readInt();
	}

	public RequestCacheUpdateServer(int bagSlot, boolean isBauble, ItemStack forStack, int preferredAmount)
	{
		this.bagSlot = bagSlot;
		this.isBauble = isBauble;
		this.forStack = forStack;
		this.preferred = preferredAmount;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(bagSlot);
		buf.writeBoolean(isBauble);
		buf.writeItemStack(forStack);
		buf.writeInt(preferred);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayerEntity player = ctx.get().getSender();

				ItemStack bag = ItemStack.EMPTY;
				if (bagSlot >= 0)
				{
					if (isBauble)
					{
						if (ModList.get().isLoaded("baubles"))
						{
							bag = BaubleHelper.getBauble(player, bagSlot);
						}
					}
					else
						bag = player.inventory.getStackInSlot(bagSlot);
				}

				if (!bag.isEmpty() && bag.getItem() instanceof BuildersBagItem)
				{
					int amount = BagCache.updateCachedBagStack(bag, player, forStack, preferred);
				}

				ctx.get().setPacketHandled(true);
			});
		}
	}

}
