package tschipp.buildersbag.api.datastructures;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.datastructures.RequirementListener.RequirementItem;

public class BagInventory
{
	Map<Item, ItemHolder> inventoryItems = new HashMap<>();
	Map<Item, CreateableItemHolder> createableItems = new HashMap<>();

	private IItemHandler realInventory;
	private BagComplex complex;

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
		if(holder.getCount() == 0)
			inventoryItems.remove(item);
			
		return removed;
	}

	/**
	 * Adds an item to the physical inventory
	 */
	public void addPhysical(ItemStack stack, @Nullable BiConsumer<Item, Integer> excessHandler)
	{
		if (inventoryItems.containsKey(stack.getItem()))
		{
			ItemHolder holder = inventoryItems.get(stack.getItem());
			holder.add(stack.getCount(), excessHandler);
		}
		else
		{
			for (int i = 0; i < realInventory.getSlots(); i++)
			{
				if (realInventory.insertItem(i, stack, true) != stack)
				{
					ItemStack rest = realInventory.insertItem(i, stack, false);
					if (!rest.isEmpty())
						throw new IllegalStateException("Stack somehow wasn't empty after inserting. This shouldn't happen");

					ItemHolder holder = ItemHolder.of(realInventory, i);
					inventoryItems.put(stack.getItem(), holder);
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
	 * returns true if the Bag actually has at least the specified amount of
	 * items
	 */
	public boolean hasPhysical(Item item, int amount)
	{
		return getPhysical(item, amount) >= amount;
	}

	/**
	 * returns true if the Bag has any amount of the specified item, physical or
	 * craftable.
	 */
	public boolean has(Item item)
	{
		return hasPhysical(item, 1) || createableItems.containsKey(item);
	}

	/**
	 * Returns the amount of physical items of this type
	 */
	public int getPhysical(Item item, int amount)
	{
		return inventoryItems.containsKey(item) ? inventoryItems.get(item).getCount() : 0;
	}

	public void addCraftable(Item item, IBagModule module, RequirementItem req)
	{
		complex.notifyItemAddedWithCheck(item);
		CreateableItemHolder createable = createableItems.getOrDefault(item, new CreateableItemHolder(item, complex));
		createable.addProvider(module, req);
		createableItems.put(item, createable);
	}

	public void removeCraftable(Item item, IBagModule module, RequirementItem req)
	{
		CreateableItemHolder createable = createableItems.getOrDefault(item, new CreateableItemHolder(item, complex));
		if (createable.removeProvider(module, req))
		{
			createableItems.remove(item);
			complex.notifyItemRemoved(item);
		}
	}

}
