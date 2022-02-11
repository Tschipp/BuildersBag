package tschipp.buildersbag.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;

public class IngredientKey implements Iterable<Item>
{
	private Set<Item> items;

	private IngredientKey()
	{
	};

	public static IngredientKey of(Item... items)
	{
		IngredientKey k = new IngredientKey();

		Preconditions.checkNotNull(items);
		Preconditions.checkArgument(items.length > 0);

		k.items = Collections.unmodifiableSet(Arrays.stream(items).collect(Collectors.toSet()));

		return k;
	}
	
	public static IngredientKey of(Ingredient ing)
	{
		return IngredientKey.of(Arrays.stream(ing.getItems()).map(stack -> stack.getItem()).toArray(Item[]::new));
	}

	
	public static int hash(Item... items)
	{
		return hash(Arrays.stream(items).collect(Collectors.toSet()));
	}
	
	public static int hash(Set<Item> items)
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}
	
	public List<Item> sorted(BagComplex complex)
	{
		BagInventory inv = complex.getInventory();
		return items.stream().sorted((o1, o2) -> {
			return Integer.compare(inv.getPhysical(o2), inv.getPhysical(o1));
		}).collect(Collectors.toList());
	}
	
	@Override
	public int hashCode()
	{
		return hash(items);
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
		IngredientKey other = (IngredientKey) obj;
		if (items == null)
		{
			if (other.items != null)
				return false;
		}
		else if (!items.equals(other.items))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return items.toString();
	}

	@Override
	public Iterator<Item> iterator()
	{
		return items.iterator();
	}

}
