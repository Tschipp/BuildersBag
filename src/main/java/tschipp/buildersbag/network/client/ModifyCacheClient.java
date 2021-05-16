package tschipp.buildersbag.network.client;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.network.NetworkMessage;

public class ModifyCacheClient implements NetworkMessage
{
	private int bagSlot;
	private boolean isBauble;
	private ItemStack forStack;
	private int amount;
	
	public ModifyCacheClient(PacketBuffer buf)
	{
		bagSlot = buf.readInt();
		isBauble = buf.readBoolean();
		forStack = buf.readItem();
		amount = buf.readInt();
	}

	public ModifyCacheClient(int bagSlot, boolean isBauble, ItemStack forStack, int amount)
	{
		this.bagSlot = bagSlot;
		this.isBauble = isBauble;
		this.forStack = forStack;
		this.amount = amount;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(bagSlot);
		buf.writeBoolean(isBauble);
		buf.writeItem(forStack);
		buf.writeInt(amount);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = BuildersBag.proxy.getPlayer();
				
				ItemStack bag = ItemStack.EMPTY;
				if (isBauble)
				{
					if (ModList.get().isLoaded("baubles"))
					{
						bag = BaubleHelper.getBauble(player, bagSlot);
					}
				}
				else
					bag = player.inventory.getItem(bagSlot);
				
				BagCache.modifyCachedAmount(bag, forStack, amount);
				
				ctx.get().setPacketHandled(true);
			});
		}
	}
}
