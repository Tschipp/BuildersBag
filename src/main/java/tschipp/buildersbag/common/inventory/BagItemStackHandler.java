package tschipp.buildersbag.common.inventory;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
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
		return stack.getItem() != null && (stack.getItem() instanceof BlockItem || stack.getItem() instanceof IBlockSource || BlockSourceAdapterHandler.hasAdapter(stack) || stack.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("littletiles:blockingredient")));
	}
}
