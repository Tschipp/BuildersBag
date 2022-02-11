package tschipp.buildersbag.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Testing
{
	public static void assertBagCorrectness(IBagCap bag)
	{
		assertInventory(bag);
		assertCreatable(bag);
	}
	
	public static void assertInventory(IBagCap bag)
	{
		BagInventory inv = bag.getComplex().getInventory();
		
		Map<Item, Integer> contents = countItems(inv);
		
		for(Entry<Item, Integer> e : contents.entrySet())
		{
			if(!inv.inventoryItems.containsKey(e.getKey()))
				throw new TestingException("Virtual inventory didn't contain " + e.getKey() + " from real inventory");
			
			if(inv.inventoryItems.get(e.getKey()).getCount() != e.getValue())
				throw new TestingException("Virtual inventory didn't contain the same amount of " + e.getKey() + ". It contained " + inv.inventoryItems.get(e.getKey()).getCount() + " instead of " + e.getValue());

		}
		
		for(Entry<Item, ItemHolder> e : inv.inventoryItems.entrySet())
		{
			if(!contents.containsKey(e.getKey()))
				throw new TestingException("Phyiscal inventory didn't contain " + e.getKey() + " from virtual inventory");
			
			if(contents.get(e.getKey()) != e.getValue().getCount())
				throw new TestingException("Phyiscal inventory didn't contain the same amount of " + e.getKey() + ". It contained " + contents.get(e.getKey()) + " instead of " + e.getValue().getCount());

		}
	}
	
	private static Map<Item, Integer> countItems(BagInventory inv)
	{
		Map<Item, Integer> contents = new HashMap<>();
		
		for (int i = 0; i < inv.realInventory.getSlots(); i++)
		{
			ItemStack inSlot = inv.realInventory.getStackInSlot(i);
			Item slotItem = inSlot.getItem();
			if (!inSlot.isEmpty())
			{
				int val = contents.getOrDefault(slotItem, 0);
				contents.put(slotItem, val+inSlot.getCount());
			}
		}
		return contents;
	}

	public static void assertCreatable(IBagCap bag)
	{
		
	}
	
	public static void assertHolderCorrectness(BagInventory inv, Item item)
	{
		Map<Item, Integer> contents = countItems(inv);

		int amount = contents.containsKey(item) ? contents.get(item) : 0;
		
		if(!inv.inventoryItems.containsKey(item) && amount != 0)
			throw new TestingException("Holder for " + item + " didn't exist in virtual inventory");
		
		if(inv.inventoryItems.containsKey(item) && inv.inventoryItems.get(item).getCount() != amount)
			throw new TestingException("Holder for " + item + " contained the wrong amount! It contained " + inv.inventoryItems.get(item).getCount() + " instead of " + amount);

		
	}
	
	public static class TestingException extends RuntimeException
	{
		private static final long serialVersionUID = 2383829541797619727L;
	
		public TestingException(String str)
		{
			super(str);
		}
		
	}

	
}
