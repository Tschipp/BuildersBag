package tschipp.buildersbag.api.datastructures;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import tschipp.buildersbag.common.crafting.CraftingHandler;

public class ItemContainer
{
	private static final Map<String, ItemContainer> cache = new HashMap<String, ItemContainer>();
	
	private ItemStack item;
	private String ingString;
	private boolean isIng;
	
	public static ItemContainer forStack(ItemStack stack)
	{
		String itemString = CraftingHandler.getItemString(stack);
		if(cache.containsKey(itemString))
			return cache.get(itemString);
		
		cache.put(itemString, new ItemContainer(stack, itemString));
		return cache.get(itemString);
	}
	
	public static ItemContainer forIngredient(String ingredientString)
	{
		if(cache.containsKey(ingredientString))
			return cache.get(ingredientString);
		
		cache.put(ingredientString, new ItemContainer(CraftingHandler.getItemFromString(ingredientString.split(";")[0] + ";"), ingredientString));
		return cache.get(ingredientString);
	}
	
	private ItemContainer(ItemStack stack, String ing)
	{
		this.ingString = ing;
		this.item = stack;
		this.isIng = ingString.split(";").length > 1;
	}

	public ItemStack getItem()
	{
		return item.copy();
	}
	
	public String getString()
	{
		return ingString;
	}
	
	public boolean isIngredient()
	{
		return isIng;
	}
	
	public ItemContainer[] getItems()
	{
		String[] split = ingString.split(";");
		ItemContainer[] items = new ItemContainer[split.length];
		for(int i = 0; i < split.length; i++)
		{
			items[i] = ItemContainer.forStack(CraftingHandler.getItemFromString(split[i] + ";"));
		}
		
		return items;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ingString == null) ? 0 : ingString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemContainer other = (ItemContainer) obj;
		if (ingString == null)
		{
			if (other.ingString != null)
				return false;
		}
		else if (!ingString.equals(other.ingString))
			return false;
		if (item == null)
		{
			if (other.item != null)
				return false;
		}
		else if (!ItemStack.isSame(item, other.item))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
//		return this.isIngredient() ? ingString : item.getItem().getRegistryName() + "@" + item.getMetadata();
		return null; //obsolete, remove
	}
	
	
}
