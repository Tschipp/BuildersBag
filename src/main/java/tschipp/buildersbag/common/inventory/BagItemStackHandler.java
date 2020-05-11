package tschipp.buildersbag.common.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBlockSource;
import tschipp.buildersbag.compat.blocksourceadapter.BlockSourceAdapterHandler;

public class BagItemStackHandler extends ItemStackHandler
{

	
	public BagItemStackHandler(int slots)
	{
		super(slots);
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return stack.getItem() instanceof ItemBlock || stack.getItem() instanceof IBlockSource || BlockSourceAdapterHandler.hasAdapter(stack) || stack.getItem() == Item.getByNameOrId("littletiles:blockingredient");
	}
}
