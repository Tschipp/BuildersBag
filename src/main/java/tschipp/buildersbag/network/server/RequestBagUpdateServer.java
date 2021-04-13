package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.network.NetworkMessage;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;

public class RequestBagUpdateServer implements NetworkMessage
{

	public int slot;
	public boolean isBauble;

	public RequestBagUpdateServer(PacketBuffer buf)
	{
		slot = buf.readInt();
		isBauble = buf.readBoolean();
	}

	public RequestBagUpdateServer(int slot, boolean isBauble)
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

				ItemStack bag = InventoryHelper.getStackInSlot(player, slot, isBauble);
				IBagCap cap = CapHelper.getBagCap(bag);
				BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncBagCapInventoryClient(cap, slot, isBauble));
				
				ctx.get().setPacketHandled(true);
			});
		}
	}

}
