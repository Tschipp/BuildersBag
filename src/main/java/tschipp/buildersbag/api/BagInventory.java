package tschipp.buildersbag.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.RequirementListener.ItemCreationRequirements;

public class BagInventory
{
	Map<Item, ItemHolder> inventoryItems = new HashMap<>();
	Map<Item, CreateableItemHolder> createableItems = new HashMap<>();

	IItemHandler realInventory;
	private BagComplex complex;
	Set<Item> pendingCreations = new HashSet<>();
	Item creationStart = null;

	public BagInventory(IItemHandler inv, BagComplex bagComplex)
	{
		if (inv == null)
			throw new IllegalArgumentException("Passed Bag Inventory was null!");

		this.realInventory = inv;
		this.complex = bagComplex;
		initialize();
	}

	private void initialize()
	{
		for (int i = 0; i < realInventory.getSlots(); i++)
		{
			ItemStack inSlot = realInventory.getStackInSlot(i);
			Item slotItem = inSlot.getItem();
			if (!inSlot.isEmpty())
			{
				ItemHolder holder = null;
				if (inventoryItems.containsKey(slotItem))
				{
					holder = inventoryItems.get(inSlot.getItem());
					holder.addSlot(realInventory, i);
				}
				else
				{
					holder = ItemHolder.of(realInventory, i);
				}

				inventoryItems.put(slotItem, holder);
			}
		}
	}

	void initCreateable()
	{
		for (ItemHolder holder : inventoryItems.values())
		{
			complex.notifyItemAdded(holder.getItem());
		}
	}

	/**
	 * Tries to remove the provided amount of items from the inventory (actually
	 * removes them). The amount returned is the amount that was removed.
	 */
	public int removePhysical(Item item, int amount)
	{
		ItemHolder holder = inventoryItems.get(item);
		if (holder == null)
			return 0;

		int removed = holder.remove(amount);
		if (holder.getCount() == 0)
			inventoryItems.remove(item);

		if (BuildersBag.TESTING)
			Testing.assertHolderCorrectness(this, item);

		return removed;
	}

	public int tryCreating(Item item, int amount, PlayerEntity player)
	{
		try
		{
			BuildersBag.LOGGER.info("Trying to create " + amount + " of " + item);

			CreateableItemHolder createable = createableItems.get(item);
			if (createable == null)
				return 0;

			if (creationStart == null)
				creationStart = item;

			if (!pendingCreations.contains(item))
			{
				pendingCreations.add(item);
				int created = createable.create(amount, player);
				if (created > 0)
					pendingCreations.remove(item);
				return created;
			}
			else
			{
				BuildersBag.LOGGER.info("Blocked creation of " + item + " because creation was pending");
			}

			return 0;
		}
		finally
		{
			if(creationStart == item)
			{
				pendingCreations.clear();
				creationStart = null;
			}
		}
	}

	/**
	 * Adds an item to the physical inventory
	 */
	public void addPhysical(ItemStack stack, @Nullable BiConsumer<Item, Integer> excessHandler)
	{
		if (stack.isEmpty())
			return;

		if (inventoryItems.containsKey(stack.getItem()))
		{
			ItemHolder holder = inventoryItems.get(stack.getItem());
			holder.add(stack.getCount(), excessHandler);

			if (BuildersBag.TESTING)
				Testing.assertHolderCorrectness(this, stack.getItem());
		}
		else
		{
			for (int i = 0; i < realInventory.getSlots(); i++)
			{
				if (!ItemStack.isSame(realInventory.insertItem(i, stack, true), stack))
				{
					ItemStack rest = realInventory.insertItem(i, stack, false);
					if (!rest.isEmpty())
						throw new IllegalStateException("Stack somehow wasn't empty after inserting. This shouldn't happen");

					ItemHolder holder = ItemHolder.of(realInventory, i);
					inventoryItems.put(stack.getItem(), holder);

					if (BuildersBag.TESTING)
						Testing.assertHolderCorrectness(this, stack.getItem());

					return;
				}
			}

			excessHandler.accept(stack.getItem(), stack.getCount());
		}
	}

	/**
	 * Adds an item to the physical inventory
	 */
	@SuppressWarnings("deprecation")
	public void addPhysical(Item item, int amount, @Nullable BiConsumer<Item, Integer> excessHandler)
	{
		while (amount > 0)
		{
			ItemStack stack = new ItemStack(item, Math.min(amount, item.getMaxStackSize()));
			amount -= Math.min(amount, item.getMaxStackSize());
			addPhysical(stack, excessHandler);
		}
	}

	/**
	 * returns true if the Bag can create a given item
	 */
	public boolean hasCraftable(Item item)
	{
		return createableItems.containsKey(item);
	}

	/**
	 * returns true if the Bag actually has at least the specified amount of
	 * items
	 */
	public boolean hasPhysical(Item item, int amount)
	{
		return getPhysical(item) >= amount;
	}

	/**
	 * returns true if the Bag has any amount of the specified item, physical or
	 * craftable.
	 */
	public boolean has(Item item)
	{
		return hasPhysical(item, 1) || hasCraftable(item);
	}

	/**
	 * Returns the amount of physical items of this type
	 */
	public int getPhysical(Item item)
	{
		return inventoryItems.containsKey(item) ? inventoryItems.get(item).getCount() : 0;
	}

	public void addCraftable(Item item, IBagModule module, ItemCreationRequirements req)
	{
		CreateableItemHolder createable = createableItems.getOrDefault(item, new CreateableItemHolder(item, complex));
		if (createable.addProvider(module, req))
			complex.notifyItemAddedWithCheck(item);
		createableItems.put(item, createable);
	}

	public void removeCraftable(Item item, IBagModule module, ItemCreationRequirements req)
	{
		CreateableItemHolder createable = createableItems.getOrDefault(item, new CreateableItemHolder(item, complex));
		if (createable.removeProvider(module, req))
		{
			createableItems.remove(item);
			complex.notifyItemRemoved(item);
		}
	}

	public boolean isCraftableExcept(Item item, IBagModule module)
	{
		CreateableItemHolder createable = createableItems.getOrDefault(item, new CreateableItemHolder(item, complex));
		IBagModule nextClosest = createable.getClosestCreationModuleExcept(module);
		return nextClosest != null;
	}



}
