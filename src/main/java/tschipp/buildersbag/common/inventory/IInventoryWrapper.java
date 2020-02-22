package tschipp.buildersbag.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandlerModifiable;

public class IInventoryWrapper implements IInventory
{

	private IItemHandlerModifiable handler;
	private String name;
	
	public IInventoryWrapper(IItemHandlerModifiable itemHandler, String name)
	{
		this.handler = itemHandler;
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(name);
	}

	@Override
	public int getSizeInventory()
	{
		return handler.getSlots();
	}

	@Override
	public boolean isEmpty()
	{
		for(int i = 0; i < handler.getSlots(); i++)
			if(!handler.getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		return handler.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		ItemStack stack = handler.getStackInSlot(index);
		stack.shrink(count);
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		ItemStack old = handler.getStackInSlot(index);
		handler.setStackInSlot(index, ItemStack.EMPTY);
		return old;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		handler.setStackInSlot(index, stack);
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
		return handler.isItemValid(index, stack);
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
		for(int i = 0; i < handler.getSlots(); i++)
			handler.setStackInSlot(i, ItemStack.EMPTY);
	}

}
