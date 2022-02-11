package tschipp.buildersbag.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import tschipp.buildersbag.BuildersBag;

public class ItemHolder
{
	private IItemHandler handler;
	private List<Integer> slots = new ArrayList<>();
	private Item containedItem;
	private int containedAmount;
	
	private ItemHolder() {};
	
	public static ItemHolder of(IItemHandler handler, int slot)
	{		
		if(handler == null)
			BuildersBag.LOGGER.error("ItemHandler was null!");
		
		ItemHolder holder = new ItemHolder();
		holder.handler = handler;
		ItemStack inSlot = handler.getStackInSlot(slot);
		
		if(inSlot.isEmpty())
			BuildersBag.LOGGER.error("Slot was empty!");
		
		holder.containedAmount = inSlot.getCount();
		holder.containedItem = inSlot.getItem();
		holder.slots.add(slot);
		
		return holder;
	}
	
	public void addSlot(IItemHandler handler, int slot)
	{
		ItemStack inSlot = handler.getStackInSlot(slot);
		if(inSlot.isEmpty())
			return;
		
		if(inSlot.getItem() != containedItem)
			throw new IllegalArgumentException("Not same item!");
		
		containedAmount += inSlot.getCount();
		slots.add(slot);
	}
	
	public int remove(int amount)
	{
		int removed = 0;
		
		for(int slot : slots)
		{
			ItemStack extracted = handler.extractItem(slot, amount, false);
			removed += extracted.getCount();
			amount -= extracted.getCount();
			
			if(amount == 0)
				break;
		}
		slots.removeIf(slot -> handler.getStackInSlot(slot).isEmpty());
		
		if(slots.isEmpty())
			amount = 0;
		
		containedAmount -= removed;
		
		return removed;
	}
	
	public void add(int amount, @Nullable BiConsumer<Item, Integer> excessHandler)
	{
		if(amount <= 0)
			return;
		
		ItemStack insertionStack = new ItemStack(containedItem, Math.min(amount, 64));
		amount -= insertionStack.getCount();
		
		for(int slot : slots)
		{
			if(handler.insertItem(slot, insertionStack, true) != insertionStack)
			{
				ItemStack rest = handler.insertItem(slot, insertionStack, false);
				if(!rest.isEmpty())
					amount += rest.getCount();
				
				if(amount == 0)
					return;
				
				insertionStack = new ItemStack(containedItem, Math.min(amount, 64));
				amount -= insertionStack.getCount();
			}
		}
		
		for (int i = 0; i < handler.getSlots(); i++)
		{
			if(handler.insertItem(i, insertionStack, true) != insertionStack)
			{
				ItemStack rest = handler.insertItem(i, insertionStack, false);
				if(!rest.isEmpty())
					amount += rest.getCount();
				
				if(amount == 0)
					return;
				
				insertionStack = new ItemStack(containedItem, Math.min(amount, 64));
				amount -= insertionStack.getCount();
			}
		}
		
		if(amount != 0 && excessHandler != null)
			excessHandler.accept(containedItem, amount);
	}
	
	public int getCount()
	{
		return containedAmount;
	}
	
	public Item getItem()
	{
		return containedItem;
	}
	
	public IItemHandler getHandler()
	{
		return handler;
	}
	
	public boolean has(Item item, int amount)
	{
		return containedItem == item && containedAmount >= amount;
	}
	
	public boolean isItem(Item item)
	{
		return containedItem == item;
	}
	
	@Override
	public String toString()
	{
		return containedAmount + " x " + containedItem;
	}
}
