package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.network.NetworkMessage;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;

public class CompactBagServer implements NetworkMessage
{

	public int slot;
	public boolean isBauble;
	
	public CompactBagServer(PacketBuffer buf)
	{
		slot = buf.readInt();
		isBauble = buf.readBoolean();
	}
	
	public CompactBagServer(int slot)
	{
		this(slot, false);
	}
	
	public CompactBagServer(int slot, boolean isBauble)
	{
		this.slot = slot;
		this.isBauble = isBauble;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(slot);
		buf.writeBoolean(isBauble);
	}
	

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayerEntity player = ctx.get().getSender();

				if (slot >= 0)
				{
					ItemStack stack = ItemStack.EMPTY;
					if (isBauble)
					{
						if (ModList.get().isLoaded("baubles"))
						{
							stack = BaubleHelper.getBauble(player,slot);
						}
					} else
						stack = player.inventory.getItem(slot);

					if (!stack.isEmpty())
					{
						IBagCap oldCap = CapHelper.getBagCap(stack);
						
						BagHelper.compactStacks(oldCap, player);
						BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncBagCapInventoryClient(oldCap, slot, isBauble));
					}
				}

				ctx.get().setPacketHandled(true);
			});
		}
	}
}
