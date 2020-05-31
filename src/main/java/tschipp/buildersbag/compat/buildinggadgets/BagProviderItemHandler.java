package tschipp.buildersbag.compat.buildinggadgets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;

public class BagProviderItemHandler extends ItemStackHandler
{
	private ItemStack bag = ItemStack.EMPTY;
	
	private ItemStack lastExtractedItem = ItemStack.EMPTY;
	
	public BagProviderItemHandler(ItemStack bag)
	{
		this.bag = bag;
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return super.extractItem(slot, amount, simulate);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return stack;
	}
	
	@Override
	public int getSlots()
	{
		return stacks.size();
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return false;
	}
	
	public void updatePossibleItems(EntityPlayer player)
	{
		IBagCap cap = CapHelper.getBagCap(bag);
		this.stacks = BagHelper.getAllAvailableStacks(cap, player);
	}
	
}
