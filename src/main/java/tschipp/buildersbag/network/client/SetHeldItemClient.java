package tschipp.buildersbag.network.client;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.network.NetworkMessage;

public class SetHeldItemClient implements NetworkMessage
{

	public ItemStack stack;
	public boolean right;
	public String bag;

	public SetHeldItemClient(PacketBuffer buf)
	{
		stack = buf.readItem();
		right = buf.readBoolean();
	}

	public SetHeldItemClient(ItemStack stack, Hand hand)
	{
		this.stack = stack;
		this.bag = stack.getItem().getRegistryName().toString();
		this.right = hand == Hand.MAIN_HAND;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeItem(stack);
		buf.writeBoolean(right);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				PlayerEntity player = BuildersBag.proxy.getPlayer();
				Hand hand = right ? Hand.MAIN_HAND : Hand.OFF_HAND;

				player.setItemInHand(hand, stack);

				ctx.get().setPacketHandled(true);
			});
		}
	}

}
