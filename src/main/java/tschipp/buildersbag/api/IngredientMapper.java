package tschipp.buildersbag.api;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class IngredientMapper
{
	private static final Multimap<Item, IngredientKey> map = HashMultimap.create();
	private static boolean init = false;
	
	public static void addMapping(Item item, Item... aliases)
	{
		IngredientKey key = IngredientKey.of(aliases);
		addMapping(item, key);
	}
	
	public static void addMapping(Item item, IngredientKey key)
	{		
		if(!map.containsEntry(item, key))
		{
			map.put(item, key);
		}
	}
	
	public static Collection<IngredientKey> getKeys(Item item)
	{
		return map.get(item);
	}
	
	public static void init()
	{
		if(init)
			return;
		
		for(Item item : ForgeRegistries.ITEMS.getValues())
		{
			map.put(item, IngredientKey.of(item));
		}
	}
}
