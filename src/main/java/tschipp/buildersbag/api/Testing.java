package tschipp.buildersbag.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.codehaus.plexus.util.FileUtils;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.RequirementListener.ItemCreationRequirements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Testing
{	
//	@Test
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
		BuildersBag.TESTING = false;
		BagComplex reference = new BagComplex(bag);
		BagInventory ri = reference.getInventory();
		BagInventory ti = bag.getComplex().getInventory();
		Map<Item, CreateableItemHolder> refCreateable = ri.createableItems;
		Map<Item, CreateableItemHolder> createable = ti.createableItems;
		BuildersBag.TESTING = true;

		for(Entry<Item, CreateableItemHolder> entry : refCreateable.entrySet())
		{
			if(!createable.containsKey(entry.getKey()))
			{
				printCreateableGraph(ri, "reference");
				printCreateableGraph(ti, "tested");
				throw new TestingException("Createable Holder for " + entry.getKey() + " should have existed in tested bag, but didn't");
			}
		}
		
		for(Entry<Item, CreateableItemHolder> entry : createable.entrySet())
		{
			if(!refCreateable.containsKey(entry.getKey()))
			{
				printCreateableGraph(ri, "reference");
				printCreateableGraph(ti, "tested");
				throw new TestingException("Createable Holder for " + entry.getKey() + " existed in tested bag, but shouldn't");	
			}
		}
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
	
	private static void printCreateableGraph(BagInventory inv, String name)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("digraph g1 {\n");

		sb.append("size=\"30,10\";\r\n" 
		   + "    page=\"8.5,11\";\r\n" 
		   + "    ratio=fill;\n "
		   + "	  node [ shape = rect, width = 0.7 ];");
				
		for (Entry<Item, CreateableItemHolder> entry : inv.createableItems.entrySet())
		{
			CreateableItemHolder holder = entry.getValue();
			ItemCreationRequirements req = holder.getClosestProvider();
			
			for(IngredientKey ing : req.getRequirements())
			{
				for(Item it : ing)
					sb.append("\"" + it + "\" -> \"" + req.getOutput() + "\"\n");
			}
		}

		for(Entry<Item, ItemHolder> entry  : inv.inventoryItems.entrySet())
		{
			Item it = entry.getKey();
			sb.append("\" Physical: " + it + "\" -> \"" + it + "\"\n");
		}

		sb.append("}");
		try
		{
			FileUtils.fileWrite(new File(name + ".txt"), sb.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static String wrap(String in)
	{
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < in.length(); i++)
		{
			out.append(in.charAt(i));
			if(i % 20 == 0 && i != 0)
				out.append("\\n");
		}
		return out.toString();
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
