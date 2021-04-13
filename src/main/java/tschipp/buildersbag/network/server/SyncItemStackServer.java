package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.network.NetworkMessage;

public class SyncItemStackServer implements NetworkMessage
{

	private ItemStack stack;
	private boolean right;

	public SyncItemStackServer(PacketBuffer buf)
	{
		stack = buf.readItemStack();
		right = buf.readBoolean();
	}

	public SyncItemStackServer(ItemStack stack, Hand hand)
	{
		this.stack = stack;
		this.right = hand == Hand.MAIN_HAND;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeItemStack(stack);
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
				stack.deserializeNBT(stack.serializeNBT());

				ctx.get().setPacketHandled(true);
			});
		}
	}
}
