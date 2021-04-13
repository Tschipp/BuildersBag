package tschipp.buildersbag.api.datastructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.item.Item;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.datastructures.RequirementListener.RequirementItem;

public class CreateableItemsManager
{
	private Set<Item> currentlyCreateable = new HashSet<>();
	private Multimap<Item, RequirementItem> currentlyProvidedFrom = HashMultimap.create();
	private Map<RequirementItem, Tracker> activeRequirements = new HashMap<>();
	private IBagModule module;
	
	public CreateableItemsManager(IBagModule module)
	{
		this.module = module;
	}
	
	/*
	 * Marks that item can be created somehow, keep track on what requirement(s) it is based, sends notifications if new items are created.
	 */
	public void add(BagComplex complex, RequirementItem req, Item item)
	{
		Tracker track = activeRequirements.getOrDefault(req, new Tracker(req));
		if(track.add(item))
		{
			currentlyCreateable.add(req.getOutput());
			currentlyProvidedFrom.put(req.getOutput(), req);
			complex.getInventory().addCraftable(req.getOutput(), module, req);
		}
	}
	
	public void remove(BagComplex complex, RequirementItem req, Item item)
	{
		if(!activeRequirements.containsKey(req))
			return;
		
		Tracker track = activeRequirements.get(req);
		if(track.remove(item))
		{
			activeRequirements.remove(req);
			currentlyProvidedFrom.remove(req.getOutput(), req);
			if(currentlyProvidedFrom.get(req.getOutput()).isEmpty())
			{
				currentlyCreateable.remove(req.getOutput());
				complex.getInventory().removeCraftable(req.getOutput(), module, req);
			}
		}
	}
	
	static class Tracker
	{
		
		private final Set<Item> requirements;
		private final Set<Item> active = new HashSet<Item>();
		
		public Tracker(RequirementItem req)
		{
			requirements = req.getRequirements();
		}
		
		/*
		 * marks item as available. Returns true if all are available
		 */
		public boolean add(Item item)
		{
			active.add(item);
			if(active.containsAll(requirements))
				return true;
			return false;
		}
		
		/*
		 * mark item as unavailable. Returns true if none are available.
		 */
		public boolean remove(Item item)
		{
			active.remove(item);
			if(active.isEmpty())
				return true;
			return false;
		}
	}
}
