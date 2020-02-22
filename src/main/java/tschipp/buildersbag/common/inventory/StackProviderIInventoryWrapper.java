package tschipp.buildersbag.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class StackProviderIInventoryWrapper implements IInventory
{

	private IBagCap bag;
	private ItemStack stack;
	private EntityPlayer player;
	private NonNullList<ItemStack> availableStacks;
	
	public StackProviderIInventoryWrapper(IBagCap bag, ItemStack stack, EntityPlayer player)
	{
		this.bag = bag;
		this.stack = stack;
		this.player = player;
		this.availableStacks = InventoryHelper.getAllAvailableStacks(bag);
	}

	@Override
	public String getName()
	{
		return "Builder's Bag";
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(this.getName());
	}

	@Override
	public int getSizeInventory()
	{
		return InventoryHelper.getAllAvailableStacks(bag).size();
	}

	@Override
	public boolean isEmpty()
	{
		return availableStacks.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		ItemStack provided = InventoryHelper.getOrProvideStack(availableStacks.get(index), bag, player, null);
		this.availableStacks = InventoryHelper.getAllAvailableStacks(bag);
		
		return provided;
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		ItemStack provided = getStackInSlot(index);
		provided.shrink(count);
		return provided;
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{		
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{		
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{		
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{		
	}

}
