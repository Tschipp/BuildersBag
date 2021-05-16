package tschipp.buildersbag.common.inventory;

import net.minecraft.item.BlockItem;
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
	public void set(ItemStack stack)
	{
		stack.setCount(1);
		super.set(stack);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return stack.getItem() instanceof BlockItem;
	}
	
	@Override
	public int getMaxStackSize(ItemStack stack)
	{
		return 0;
	}

}
