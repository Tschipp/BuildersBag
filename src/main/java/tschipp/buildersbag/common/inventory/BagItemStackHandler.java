package tschipp.buildersbag.common.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class BagItemStackHandler extends ItemStackHandler
{

	
	public BagItemStackHandler(int slots)
	{
		super(slots);
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return true;
//		return stack.getItem() != null && (stack.getItem() instanceof BlockItem || stack.getItem() instanceof IBlockSource || BlockSourceAdapterHandler.hasAdapter(stack) || stack.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("littletiles:blockingredient")));
	}
}
