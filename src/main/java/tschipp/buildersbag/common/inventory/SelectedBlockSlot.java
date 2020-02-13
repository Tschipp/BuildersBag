package tschipp.buildersbag.common.inventory;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SelectedBlockSlot extends SlotItemHandler
{

	public SelectedBlockSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, index, xPosition, yPosition);
	}
	
	@Override
	public void putStack(ItemStack stack)
	{
		stack.setCount(1);
		super.putStack(stack);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return stack.getItem() instanceof ItemBlock;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return 0;
	}

}
