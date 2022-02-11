package tschipp.buildersbag.network.client;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.network.NetworkMessage;

public class SetWorkStateClient implements NetworkMessage
{

	private String bag;
	private boolean start;

	public SetWorkStateClient(PacketBuffer buf)
	{
		bag = buf.readUtf();
		start = buf.readBoolean();
	}

	public SetWorkStateClient(String bag, boolean start)
	{
		this.bag = bag;
		this.start = start;
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeUtf(bag);
		buf.writeBoolean(start);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				BuildersBag.proxy.changeWorkState(bag, BuildersBag.proxy.getPlayer(), start);

			});
		}
	}
}
