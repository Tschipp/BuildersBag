package tschipp.buildersbag.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;

public class SyncEnderchestToClient implements IMessage, IMessageHandler<SyncEnderchestToClient, IMessage>
{
	private InventoryEnderChest enderchest;
	private NBTTagList nbt;

	public SyncEnderchestToClient()
	{
	}

	public SyncEnderchestToClient(PlayerEntity player)
	{
		enderchest = player.getInventoryEnderChest();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		CompoundNBT tag = ByteBufUtils.readTag(buf);
		nbt = (NBTTagList) tag.getTag("list");
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		CompoundNBT tag = new CompoundNBT();
		tag.setTag("list", enderchest.saveInventoryToNBT());
		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public IMessage onMessage(SyncEnderchestToClient message, MessageContext ctx)
	{
		final IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(() -> {

			PlayerEntity player = BuildersBag.proxy.getPlayer();
			player.getInventoryEnderChest().loadInventoryFromNBT(message.nbt);

		});

		return null;
	}

}
