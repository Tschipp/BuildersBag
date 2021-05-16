package tschipp.buildersbag.network.client;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.network.NetworkMessage;

public class SyncBagCapInventoryClient implements NetworkMessage
{

	private IBagCap bagCap;
	public int slot;
	public CompoundNBT readTag;
	public boolean isBauble;

	public SyncBagCapInventoryClient(PacketBuffer buf)
	{
		readTag = buf.readNbt();
		slot = buf.readInt();
		isBauble = buf.readBoolean();
	}

	public SyncBagCapInventoryClient(IBagCap bagCap, int slot, boolean isBauble)
	{
		if (bagCap == null || bagCap.getBlockInventory() == null)
		{
			BuildersBag.LOGGER.error("Invalid Bag Cap! It is null!");
			new Throwable().printStackTrace();
		}

		this.bagCap = bagCap;
		this.slot = slot;
		this.isBauble = isBauble;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeNbt((CompoundNBT) BagCapProvider.BAG_CAPABILITY.writeNBT(bagCap, null));
		buf.writeInt(slot);
		buf.writeBoolean(isBauble);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				PlayerEntity player = BuildersBag.proxy.getPlayer();
				if (slot >= 0)
				{
					ItemStack stack = ItemStack.EMPTY;
					if (isBauble)
					{
						if (ModList.get().isLoaded("baubles"))
						{
							stack = BaubleHelper.getBauble(player, slot);
						}
					}
					else
						stack = player.inventory.getItem(slot);

					if (!stack.isEmpty())
					{
						IBagCap oldCap = CapHelper.getBagCap(stack);
						BagCapProvider.BAG_CAPABILITY.readNBT(oldCap, null, readTag);
					}
				}

				ctx.get().setPacketHandled(true);
			});
		}
	}
	
}
