package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.network.NetworkMessage;

public class SyncBagCapServer implements NetworkMessage
{
	private IBagCap bagCap;
	public boolean right;
	public CompoundNBT readTag;

	public SyncBagCapServer(PacketBuffer buf)
	{
		readTag = buf.readCompoundTag();
		right = buf.readBoolean();
	}

	public SyncBagCapServer(IBagCap bagCap, Hand hand)
	{
		this.bagCap = bagCap;
		this.right = hand == Hand.MAIN_HAND;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeCompoundTag((CompoundNBT) BagCapProvider.BAG_CAPABILITY.writeNBT(bagCap, null));
		buf.writeBoolean(right);
	}


	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {
				
				ServerPlayerEntity player = ctx.get().getSender();
				ItemStack stack = right ? player.getHeldItemMainhand() : player.getHeldItemOffhand();

				IBagCap oldCap = CapHelper.getBagCap(stack);
				BagCapProvider.BAG_CAPABILITY.readNBT(oldCap, null, readTag);
				
				ctx.get().setPacketHandled(true);
			});
		}
	}
}

