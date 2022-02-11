package tschipp.buildersbag.network.client;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.network.NetworkMessage;

public class GrowItemClient implements NetworkMessage
{

	private int growthAmount;
	private Hand hand;

	public GrowItemClient(PacketBuffer buf)
	{
		growthAmount = buf.readInt();
		hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
	}

	public GrowItemClient(int growthAmount, Hand hand)
	{
		this.growthAmount = growthAmount;
		this.hand = hand;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(growthAmount);
		buf.writeBoolean(hand == Hand.MAIN_HAND);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = BuildersBag.proxy.getPlayer();
				ItemStack s = player.getItemInHand(hand);
				s.grow(growthAmount);
				
				ctx.get().setPacketHandled(true);
			});
		}
	}

	
}
