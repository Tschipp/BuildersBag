package tschipp.buildersbag.api;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import tschipp.buildersbag.api.RequirementListener.ItemCreationRequirements;

public class CreateableItemHolder
{
	private final Item containedItem;
	private final Set<Tuple<IBagModule, ItemCreationRequirements>> providerModules = new HashSet<>();
	private BagComplex complex;

	private final Comparator<Tuple<IBagModule, ItemCreationRequirements>> COMPARATOR = (t1, t2) -> {
		ItemCreationRequirements r1 = t1.getSecond();
		ItemCreationRequirements r2 = t2.getSecond();

		BagInventory inv = complex.getInventory();
		double p1 = 0;
		for (IngredientKey i : r1.getRequirements())
			for (Item it : i)
				if (inv.hasPhysical(it, 1))
					p1++;

		double p2 = 0;
		for (IngredientKey i : r2.getRequirements())
			for (Item it : i)
				if (inv.hasPhysical(it, 1))
					p2++;

		double percentPhysical1 = p1 / r1.getRequirements().size();
		double percentPhysical2 = p2 / r2.getRequirements().size();

		if (percentPhysical1 > percentPhysical2)
			return -1;
		else if (percentPhysical1 < percentPhysical2)
			return 1;
		else
		{
			if(r1.getRequirements().size() == r2.getRequirements().size())
				return -1 * t1.getFirst().getPriority().compareTo(t2.getFirst().getPriority());
			else
				return Integer.compare(r2.getRequirements().size(), r1.getRequirements().size());
		}
		// return 0;
	};

	private final PriorityQueue<Tuple<IBagModule, ItemCreationRequirements>> priorityQueue = new PriorityQueue<>(COMPARATOR);

	public CreateableItemHolder(Item item, BagComplex complex)
	{
		this.containedItem = item;
		this.complex = complex;
	}

	public Item getItem()
	{
		return containedItem;
	}

	public void addProvider(IBagModule module, ItemCreationRequirements req)
	{
		Tuple<IBagModule, ItemCreationRequirements> t = new Tuple<>(module, req);
		providerModules.add(t);
		if (!priorityQueue.contains(t))
			priorityQueue.add(t);
	}

	public boolean removeProvider(IBagModule module, ItemCreationRequirements req)
	{
		Tuple<IBagModule, ItemCreationRequirements> t = new Tuple<>(module, req);
		providerModules.remove(t);
		priorityQueue.remove(t);
		return providerModules.isEmpty();
	}

	/*
	 * Returns the amount that was actually created
	 */
	public int create(int amount, PlayerEntity player)
	{
		// actually call the create methods of the specified bag modules, in
		// sorted order based on how many of the requirements
		// are able to be fulfilled physically (shortest path), then module
		// priority. Precompute the sorting when adding new provider (use
		// priorityqueue)
		@SuppressWarnings("unchecked")
		Tuple<IBagModule, ItemCreationRequirements>[] arr = priorityQueue.toArray(new Tuple[priorityQueue.size()]);
		int created = 0;
		for (Tuple<IBagModule, ItemCreationRequirements> tup : arr)
		{

			created += tup.getFirst().createItems(tup.getSecond(), amount - created, complex, player);

			if (created >= amount)
				break;
		}
		return created;

	}

	@Override
	public String toString()
	{
		return "Createable: " + containedItem;
	}

}
