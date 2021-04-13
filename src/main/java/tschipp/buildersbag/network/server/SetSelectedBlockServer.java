package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.network.NetworkMessage;

public class SetSelectedBlockServer implements NetworkMessage
{

	private ItemStack sel;
	private String uuid;
	
	public SetSelectedBlockServer(PacketBuffer buf)
	{
		uuid = buf.readString();
		sel = buf.readItemStack();
	}
	
	public SetSelectedBlockServer(String uuid, ItemStack selected)
	{
		this.uuid = uuid;
		this.sel = selected;
	}
	
	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeString(uuid);
		buf.writeItemStack(sel);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayerEntity player = ctx.get().getSender();

				ItemStack main = player.getHeldItemMainhand();
				ItemStack off = player.getHeldItemOffhand();
				
				IBagCap cap;
				if((cap = CapHelper.getBagCap(main)) != null && cap.getUUID().equals(uuid))
				{
					cap.getSelectedInventory().setStackInSlot(0, sel);
				}
				else if((cap = CapHelper.getBagCap(off)) != null && cap.getUUID().equals(uuid))
				{
					cap.getSelectedInventory().setStackInSlot(0, sel);
				}

				ctx.get().setPacketHandled(true);
			});
		}
	}
}
