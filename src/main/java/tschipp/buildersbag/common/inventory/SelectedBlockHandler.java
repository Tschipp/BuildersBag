package tschipp.buildersbag.common.inventory;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class SelectedBlockHandler extends ItemStackHandler
{

	public SelectedBlockHandler(int slots)
	{
		super(slots);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(stack, 1));
		this.onContentsChanged(slot);
		
		
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		this.stacks.set(slot, ItemStack.EMPTY);

		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		boolean b = stack.getItem() instanceof ItemBlock;
		return b;
	}

}
