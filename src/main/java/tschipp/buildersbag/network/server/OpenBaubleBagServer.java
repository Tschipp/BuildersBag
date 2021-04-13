package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.network.NetworkMessage;

public class OpenBaubleBagServer implements NetworkMessage
{

	private int bagSlot;

	public OpenBaubleBagServer(PacketBuffer buf)
	{
		bagSlot = buf.readInt();
	}

	public OpenBaubleBagServer(int bagSlot)
	{
		this.bagSlot = bagSlot;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(bagSlot);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayerEntity player = ctx.get().getSender();

				player.openGui(BuildersBag.instance, 1, player.world, bagSlot, 0, 0);

				ctx.get().setPacketHandled(true);
			});
		}
	}
}
