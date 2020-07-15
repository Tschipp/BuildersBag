package tschipp.buildersbag.compat.botania;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.compat.blocksourceadapter.BlockSourceAdapterHandler;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;
import vazkii.botania.common.item.ItemEnderHand;

public class BotaniaCompat
{
	private static String lastTag = "";
	private static int lastCount = 0;
	private static ItemStack lastSelected = ItemStack.EMPTY;

	public static int getBlockCount(EntityPlayer player, ItemStack requestor, ItemStack stack, Block block, int meta)
	{
		Random rand = new Random();
		ItemStack requestedStack = new ItemStack(block, 1, meta);

		String nbt = stack.serializeNBT().toString();
		if (nbt.equals(lastTag) && ItemStack.areItemsEqual(lastSelected, requestedStack) && lastCount != 0)
			return lastCount;

		if (player.world.isRemote || (rand.nextDouble() < 1 && ItemStack.areItemsEqual(lastSelected, requestedStack) && lastCount != 0))
		{
			if (lastCount != 0)
				lastCount--;
			return lastCount;
		}

		IBagCap bag = CapHelper.getBagCap(stack);

		if (!bag.hasModuleAndEnabled("buildersbag:supplier"))
			return 0;

		NonNullList<ItemStack> provided = null;

		if (player.world.isRemote)
		{
			BagCache.startSimulation(stack);
			provided = BagHelper.getOrProvideStackWithCount(requestedStack, 500, bag, player, null);
			BagCache.startSimulation(stack);
		}
		else
			provided = BagHelper.simulateProvideStackWithCount(requestedStack, 500, stack, player, null);

		lastTag = nbt;
		lastCount = provided.size();
		lastSelected = requestedStack.copy();
		return lastCount;
	}

	public static boolean provideBlock(EntityPlayer player, ItemStack requestor, ItemStack stack, Block block, int meta, boolean doit)
	{
		ItemStack requestedStack = new ItemStack(block, 1, meta);
		IBagCap bag = CapHelper.getBagCap(stack);

		if (doit)
		{
			ItemStack provided = BagHelper.getOrProvideStack(requestedStack, bag, player, null);
			BagHelper.resetRecursionDepth(player);

			if (provided.isEmpty())
				return false;

//			BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bag, InventoryHelper.getSlotForStack(player, stack)), (EntityPlayerMP) player);
			return true;
		} else
		{
			if (player.world.isRemote)
				return BagHelper.getOrProvideStack(requestedStack, bag, player, null).isEmpty();
			return BagHelper.simulateProvideStack(requestedStack, stack, player, null);
		}
	}
	
	public static void register()
	{
		BlockSourceAdapterHandler.registerAdapter(new BotaniaAdapter());
	}
	
	public static boolean isEnderHand(ItemStack stack)
	{
		return stack.getItem() instanceof ItemEnderHand;
	}
}
