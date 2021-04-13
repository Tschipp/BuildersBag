package tschipp.buildersbag.network.server;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.network.NetworkMessage;

public class ModifyPaletteServer implements NetworkMessage
{

	private ItemStack sel;
	private String uuid;
	private boolean add;
	
	public ModifyPaletteServer(PacketBuffer buf)
	{
		uuid = buf.readString();
		sel = buf.readItemStack();
		add = buf.readBoolean();
	}
	
	public ModifyPaletteServer(String uuid, ItemStack selected, boolean add)
	{
		this.uuid = uuid;
		this.sel = selected;
		this.add = add;
	}
	
	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeString(uuid);
		buf.writeItemStack(sel);
		buf.writeBoolean(add);
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
				if((cap = CapHelper.getBagCap(main)) != null && cap.getUUID().equals(uuid));
				else if((cap = CapHelper.getBagCap(off)) != null && cap.getUUID().equals(uuid));
				
				List<ItemStack> palette = cap.getPalette();
				if(add)
					palette.add(sel.copy());
				else
				{
					for(int i = 0; i < palette.size(); i++)
					{
						if(ItemStack.areItemStacksEqual(sel, palette.get(i)))
						{
							palette.remove(i);
							break;
						}
					}
				}
				
				ctx.get().setPacketHandled(true);
				
			});
		}
	}
	
}
