package tschipp.buildersbag.common.data;

import net.minecraft.item.ItemStack;

public class ItemContainer
{
	private ItemStack item;

	public static ItemContainer forStack(ItemStack stack)
	{
		return new ItemContainer(stack);
	}
	
	private ItemContainer(ItemStack item)
	{
		this.item = item;
	}

	public ItemStack getItem()
	{
		return item.copy();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.getItem().hashCode() + item.getMetadata());
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
		return item.getItem().getRegistryName() + "@" + item.getMetadata();
	}
	
	
}
