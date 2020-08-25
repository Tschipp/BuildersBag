package tschipp.buildersbag.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.config.BuildersBagConfig;

public class PlayFailureSoundClient implements IMessage, IMessageHandler<PlayFailureSoundClient, IMessage>
{

	public PlayFailureSoundClient()
	{
	}

	@Override
	public IMessage onMessage(PlayFailureSoundClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(() ->
		{

			EntityPlayer player = BuildersBag.proxy.getPlayer();

			if (BuildersBagConfig.Settings.playFailSounds)
				player.playSound(SoundEvents.BLOCK_NOTE_BASEDRUM, 0.5f, 0.1f);

		});

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
	}

}
