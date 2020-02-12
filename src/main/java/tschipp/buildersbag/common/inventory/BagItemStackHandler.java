package tschipp.buildersbag.common.inventory;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public class BagItemStackHandler extends ItemStackHandler
{

	private int limit;
	
	public BagItemStackHandler(int slots, int stackSize)
	{
		super(slots);
		this.limit = stackSize;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return stack.getItem() instanceof ItemBlock;
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = super.serializeNBT();
		tag.setInteger("StackLimit", limit);
		return tag;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		super.deserializeNBT(nbt);
		limit = nbt.getInteger("StackLimit");
	}
}
