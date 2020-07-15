package tschipp.buildersbag.common.data;

import net.minecraft.item.ItemStack;
import tschipp.buildersbag.common.crafting.CraftingHandler;

public class ItemContainer
{
	private ItemStack item;
	private String ingString;

	public static ItemContainer forStack(ItemStack stack)
	{
		return new ItemContainer(stack, CraftingHandler.getItemString(stack));
	}
	
	public static ItemContainer forIngredient(String ingredientString)
	{
		return new ItemContainer(CraftingHandler.getItemFromString(ingredientString.split(";")[0] + ";"), ingredientString);
	}
	
	private ItemContainer(ItemStack stack, String ing)
	{
		this.ingString = ing;
		this.item = stack;
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
		return ingString.split(";").length > 1;
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
		else if (!ItemStack.areItemsEqual(item, other.item))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return this.isIngredient() ? ingString : item.getItem().getRegistryName() + "@" + item.getMetadata();
	}
	
	
}
