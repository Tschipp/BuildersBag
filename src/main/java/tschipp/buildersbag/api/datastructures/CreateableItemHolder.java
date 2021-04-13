package tschipp.buildersbag.api.datastructures;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.item.Item;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.datastructures.RequirementListener.RequirementItem;

public class CreateableItemHolder
{
	private final Item containedItem;
	private final Set<Tuple<IBagModule, RequirementItem>> providerModules = new HashSet<>(); 
	private BagComplex complex;
	
	private final Comparator<Tuple<IBagModule, RequirementItem>> COMPARATOR = (t1, t2) -> {
		RequirementItem r1 = t1.getSecond();
		RequirementItem r2 = t2.getSecond();
		
		BagInventory inv = complex.getInventory();
		double p1 = 0;
		for(Item i : r1.getRequirements())
			if(inv.hasPhysical(i, 1))
				p1++;
		
		double p2 = 0;
		for(Item i : r2.getRequirements())
			if(inv.hasPhysical(i, 1))
				p2++;
		
		double percentPhysical1 = p1 / r1.getRequirements().size();
		double percentPhysical2 = p2 / r2.getRequirements().size();

		if(percentPhysical1 > percentPhysical2)
			return -1;
		else if(percentPhysical1 < percentPhysical2)
			return 1;
		else
		{
			return -1 * t1.getFirst().getPriority().compareTo(t2.getFirst().getPriority());
		}
	};
	
	
	private final PriorityQueue<Tuple<IBagModule, RequirementItem>> priorityQueue = new PriorityQueue<>(COMPARATOR);
	
	public CreateableItemHolder(Item item, BagComplex complex)
	{
		this.containedItem = item;
		this.complex = complex;
	}
	
	public Item getItem()
	{
		return containedItem;
	}
	
	public void addProvider(IBagModule module, RequirementItem req)
	{
		Tuple<IBagModule, RequirementItem> t = new Tuple<>(module, req);
		providerModules.add(t);
		priorityQueue.add(t);
	}
	
	public boolean removeProvider(IBagModule module, RequirementItem req)
	{
		Tuple<IBagModule, RequirementItem> t = new Tuple<>(module, req);
		providerModules.remove(t);
		priorityQueue.remove(t);
		return providerModules.isEmpty();
	}
	
	/*
	 * Returns the amount that was actually created
	 */
	public int create(int amount)
	{
		//actually call the create methods of the specified bag modules, in sorted order based on how many of the requirements
		//are able to be fulfilled physically (shortest path), then module priority. Precompute the sorting when adding new provider (use priorityqueue)
		return 0;
	}
	
	
}
