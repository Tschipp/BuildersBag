package tschipp.buildersbag.network.server;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.inventory.ContainerBag;
import tschipp.buildersbag.network.NetworkMessage;

public class SyncModuleStateServer implements NetworkMessage
{

	private CompoundNBT tag;
	private String name;

	public SyncModuleStateServer(PacketBuffer buf)
	{
		tag = buf.readNbt();
		name = buf.readUtf();
	}

	public SyncModuleStateServer(String name, IBagModule module)
	{
		this.name = name;
		this.tag = module.serializeNBT();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeNbt(tag);
		buf.writeUtf(name);

	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayerEntity player = ctx.get().getSender();
				if (player.containerMenu instanceof ContainerBag)
					((ContainerBag) player.containerMenu).updateModule(name, tag);

				ctx.get().setPacketHandled(true);
			});
		}
	}
}
