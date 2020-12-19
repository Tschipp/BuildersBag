package tschipp.buildersbag.network.server;

import baubles.api.BaublesApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;

public class CompactBagServer implements IMessage, IMessageHandler<CompactBagServer, IMessage>
{

	public int slot;
	public boolean isBauble;
	
	public CompactBagServer()
	{
	}
	
	public CompactBagServer(int slot)
	{
		this(slot, false);
	}
	
	public CompactBagServer(int slot, boolean isBauble)
	{
		this.slot = slot;
		this.isBauble = isBauble;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		slot = buf.readInt();
		isBauble = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(slot);
		buf.writeBoolean(isBauble);
	}
	
	@Override
	public IMessage onMessage(CompactBagServer message, MessageContext ctx)
	{
		final IThreadListener mainThread = (IThreadListener)ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(() -> {

			PlayerEntity player = ctx.getServerHandler().player;
			if (message.slot >= 0)
			{
				ItemStack stack = ItemStack.EMPTY;
				if (message.isBauble)
				{
					if (Loader.isModLoaded("baubles"))
					{
						stack = BaublesApi.getBaubles(player).getStackInSlot(message.slot);
					}
				} else
					stack = player.inventory.getStackInSlot(message.slot);

				if (!stack.isEmpty())
				{
					IBagCap oldCap = CapHelper.getBagCap(stack);
					
					BagHelper.compactStacks(oldCap, player);
					BuildersBag.network.sendTo(new SyncBagCapInventoryClient(oldCap, message.slot, message.isBauble), (ServerPlayerEntity) player);
				}
			}
		});

		return null;
	}

}
