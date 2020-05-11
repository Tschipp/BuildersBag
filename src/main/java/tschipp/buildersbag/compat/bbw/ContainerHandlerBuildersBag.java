package tschipp.buildersbag.compat.bbw;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import portablejim.bbw.api.IContainerHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.network.SyncBagCapInventoryClient;

public class ContainerHandlerBuildersBag implements IContainerHandler
{
	private String lastTag = "";
	private int lastCount = 0;
	private ItemStack lastSelected = ItemStack.EMPTY;
	
	@Override
	public int countItems(EntityPlayer player, ItemStack stack, ItemStack inventoryStack)
	{
		String nbt = inventoryStack.serializeNBT().toString();
		if(nbt.equals(lastTag) && ItemStack.areItemsEqual(lastSelected, stack))
			return lastCount;
		
		IBagCap bag = CapHelper.getBagCap(inventoryStack);
		NonNullList<ItemStack> provided = null;

		if(player.world.isRemote)
			provided = InventoryHelper.getOrProvideStackWithCount(stack, 500, bag, player, null);
		else
			provided = InventoryHelper.simulateProvideStackWithCount(stack, 500, inventoryStack, player, null);
		
		lastTag = nbt;
		lastCount = provided.size();
		lastSelected = stack.copy();
		return lastCount;
	}

	@Override
	public boolean matches(EntityPlayer player, ItemStack stack, ItemStack inventoryStack)
	{
		if(inventoryStack.getItem() instanceof BuildersBagItem)
		{
			IBagCap bag = CapHelper.getBagCap(inventoryStack);
			if(bag.hasModuleAndEnabled("buildersbag:supplier"))
				return true;
		}
		return false;
	}

	@Override
	public int useItems(EntityPlayer player, ItemStack stack, ItemStack inventoryStack, int count)
	{
		IBagCap bag = CapHelper.getBagCap(inventoryStack);
		NonNullList<ItemStack> provided = InventoryHelper.getOrProvideStackWithCount(stack, count, bag, player, null);
		BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bag, InventoryHelper.getSlotForStack(player, inventoryStack)), (EntityPlayerMP) player);
		return count - provided.size();
	}

}
