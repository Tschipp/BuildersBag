package tschipp.buildersbag.common.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
		return stack.getItem() instanceof ItemBlock || stack.getItem() == Item.getByNameOrId("littletiles:blockingredient");
	}
}
