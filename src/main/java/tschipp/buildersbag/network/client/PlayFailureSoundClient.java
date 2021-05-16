package tschipp.buildersbag.network.client;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.config.Configs;
import tschipp.buildersbag.network.NetworkMessage;

public class PlayFailureSoundClient implements NetworkMessage
{

	public PlayFailureSoundClient(PacketBuffer buf)
	{
	}

	public PlayFailureSoundClient()
	{
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				PlayerEntity player = BuildersBag.proxy.getPlayer();

				if (Configs.Settings.playFailSounds.get())
					player.playSound(SoundEvents.NOTE_BLOCK_BASEDRUM, 0.5f, 0.1f);

				ctx.get().setPacketHandled(true);
			});
		}
	}

}
